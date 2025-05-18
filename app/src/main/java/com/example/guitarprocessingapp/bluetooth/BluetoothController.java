package com.example.guitarprocessingapp.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import com.example.guitarprocessingapp.R;
import com.example.guitarprocessingapp.ui.effects.EffectParameter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothController {

    private static final String TAG = "BluetoothController";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothController instance;

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;
    private Context appContext;

    private BluetoothController() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothController getInstance() {
        if (instance == null) {
            instance = new BluetoothController();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public Context getAppContext() {
        return appContext;
    }

    public boolean hasBluetoothPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public boolean hasScanPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public List<BluetoothDevice> getPairedDevices(Context context) {
        List<BluetoothDevice> devices = new ArrayList<>();
        if (!isBluetoothSupported() || !hasBluetoothPermission(context)) {
            return devices;
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices != null) {
            devices.addAll(bondedDevices);
        }

        return devices;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connectToDevice(BluetoothDevice device, ConnectionCallback callback) {
        disconnect();

        new Thread(() -> {
            try {
                BluetoothSocket tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                tmp.connect();
                socket = tmp;
                connectedDevice = device;
                Log.d(TAG, "Connected to device: " + device.getName());
                callback.onConnected();
            } catch (IOException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                callback.onConnectionFailed(e);
            }
        }).start();
    }

    public void disconnect() {
        new Thread(() -> {
            try {
                if (socket != null) {
                    socket.close();
                    Log.d(TAG, "Socket closed");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket: " + e.getMessage());
            } finally {
                socket = null;
                connectedDevice = null;
            }
        }).start();
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void validateGuitarProcessor(DeviceValidationCallback callback) {
        new Thread(() -> {
            if (socket == null || !socket.isConnected()) {
                callback.onValidationResult(false);
                return;
            }

            if (appContext == null) {
                Log.e(TAG, "App context not initialized");
                callback.onValidationResult(false);
                return;
            }

            String command = appContext.getString(R.string.cmd_identify_device);
            String expected = appContext.getString(R.string.resp_valid_device);

            try {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();

                out.write((command + "\n").getBytes());
                out.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String response = reader.readLine();

                callback.onValidationResult(expected.equals(response));

            } catch (IOException e) {
                Log.e(TAG, "Validation error: " + e.getMessage());
                callback.onValidationResult(false);
            }
        }).start();
    }

    public void sendCommand(String command, ResponseCallback callback) {
        new Thread(() -> {
            if (socket == null || !socket.isConnected()) {
                callback.onResponse(null);
                return;
            }

            try {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();

                out.write((command + "\n").getBytes());
                out.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String response = reader.readLine();

                callback.onResponse(response);

            } catch (IOException e) {
                Log.e(TAG, "Command send error: " + e.getMessage());
                callback.onResponse(null);
            }
        }).start();
    }

    public void sendEffectCommand(String effectName, boolean enable, ResponseCallback callback) {
        if (appContext == null) {
            callback.onResponse(null);
            return;
        }

        String prefix = enable
                ? appContext.getString(R.string.cmd_enable_effect_prefix)
                : appContext.getString(R.string.cmd_disable_effect_prefix);
        String command = prefix + effectName;

        sendCommand(command, callback);
    }

    public void sendUpdateEffectParamsCommand(String effectName, List<EffectParameter> parameters, ResponseCallback callback) {
        if (socket == null || !socket.isConnected()) {
            callback.onResponse(null);
            return;
        }

        try {
            JSONArray paramArray = new JSONArray();

            for (EffectParameter param : parameters) {
                JSONObject obj = new JSONObject();
                obj.put("name", param.getName());
                obj.put("value", param.getCurrentValue());
                paramArray.put(obj);
            }

            String prefix = appContext.getString(R.string.cmd_update_params_prefix);
            String command = prefix + effectName + "|" + paramArray.toString();

            sendCommand(command, callback);

        } catch (JSONException e) {
            Log.e(TAG, "JSON build error for effect params", e);
            callback.onResponse(null);
        }
    }

    public void sendEffectParamsIndividually(String effectName, List<EffectParameter> parameters, ResponseCallback callback) {
        if (socket == null || !socket.isConnected()) {
            callback.onResponse(null);
            return;
        }

        new Thread(() -> {
            try {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String lastResponse = null;

                String paramPrefix = appContext.getString(R.string.cmd_set_param_prefix);

                for (EffectParameter param : parameters) {
                    String paramCommand = paramPrefix + effectName + "|" + param.getName() + "=" + param.getCurrentValue();
                    out.write((paramCommand + "\n").getBytes());
                    out.flush();
                    lastResponse = reader.readLine();
                }

                callback.onResponse(lastResponse);

            } catch (IOException e) {
                Log.e(TAG, "Failed to send parameters individually: " + e.getMessage());
                callback.onResponse(null);
            }
        }).start();
    }

    public interface ConnectionCallback {
        void onConnected();
        void onConnectionFailed(Exception e);
    }

    public interface DeviceValidationCallback {
        void onValidationResult(boolean isGuitarProcessor);
    }

    public interface ResponseCallback {
        void onResponse(String response);
    }
}

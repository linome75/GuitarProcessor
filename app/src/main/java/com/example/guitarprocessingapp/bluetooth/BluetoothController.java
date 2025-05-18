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
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public boolean hasScanPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED;
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
                Log.d(TAG, "Connected to device: " + device.getName() + " (" + device.getAddress() + ")");
                callback.onConnected();
            } catch (IOException e) {
                Log.e(TAG, "Connection error: " + e.getMessage());
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

    public interface ConnectionCallback {
        void onConnected();
        void onConnectionFailed(Exception e);
    }

    public interface DeviceValidationCallback {
        void onValidationResult(boolean isGuitarProcessor);
    }

    /**
     * Проверяет, является ли подключенное устройство гитарным процессором.
     */
    public void validateGuitarProcessor(DeviceValidationCallback callback) {
        new Thread(() -> {
            if (socket == null || !socket.isConnected()) {
                callback.onValidationResult(false);
                return;
            }

            try {
                if (appContext == null) {
                    Log.e(TAG, "Context not initialized! Call initialize(context) first.");
                    callback.onValidationResult(false);
                    return;
                }

                String command = appContext.getString(R.string.cmd_identify_device);
                String expectedResponse = appContext.getString(R.string.resp_valid_device);

                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();

                out.write((command + "\n").getBytes());
                out.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String response = reader.readLine();

                boolean isValid = expectedResponse.equals(response);
                callback.onValidationResult(isValid);

            } catch (IOException e) {
                Log.e(TAG, "Validation error: " + e.getMessage());
                callback.onValidationResult(false);
            }
        }).start();
    }

    public interface ResponseCallback {
        void onResponse(String response);
    }

    /**
     * Универсальный метод отправки команды и получения ответа.
     */
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
                Log.e(TAG, "Send command error: " + e.getMessage());
                callback.onResponse(null);
            }
        }).start();
    }

    /**
     * Метод отправки команды включения/выключения эффекта.
     */
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
}

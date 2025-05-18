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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Singleton-класс для управления Bluetooth-соединением.
 */
public class BluetoothController {

    private static final String TAG = "BluetoothController";

    // Стандартный UUID для Serial Port Profile (SPP)
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothController instance;

    private final BluetoothAdapter bluetoothAdapter;

    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;

    private BluetoothController() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothController getInstance() {
        if (instance == null) {
            instance = new BluetoothController();
        }
        return instance;
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

    /**
     * Подключается к устройству в отдельном потоке.
     * Требуется разрешение BLUETOOTH_CONNECT (Android 12+)
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connectToDevice(BluetoothDevice device, ConnectionCallback callback) {
        disconnect();

        new Thread(() -> {
            try {
                // Создаем незащищенный RFCOMM сокет (обычно используется SPP)
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
}

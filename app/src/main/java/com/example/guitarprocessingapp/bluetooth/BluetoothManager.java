package com.example.guitarprocessingapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothManager instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;

    public static BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
        }
        return instance;
    }

    public interface ConnectionCallback {
        void onConnected();
        void onConnectionFailed(Exception e);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void connectToDevice(Context context, BluetoothDevice device, ConnectionCallback callback) {
        disconnect(); // Закрыть предыдущий сокет, если есть

        executor.execute(() -> {
            try {
                BluetoothSocket tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                tmp.connect(); // Блокирующий вызов
                socket = tmp;
                connectedDevice = device;

                Log.d(TAG, "Соединено с: " + device.getName());
                callback.onConnected();
            } catch (IOException e) {
                Log.e(TAG, "Ошибка подключения: " + e.getMessage());
                callback.onConnectionFailed(e);
            }
        });
    }

    public void disconnect() {
        executor.execute(() -> {
            try {
                if (socket != null) {
                    socket.close();
                    Log.d(TAG, "Соединение закрыто");
                }
            } catch (IOException e) {
                Log.e(TAG, "Ошибка при закрытии сокета: " + e.getMessage());
            } finally {
                socket = null;
                connectedDevice = null;
            }
        });
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }
}

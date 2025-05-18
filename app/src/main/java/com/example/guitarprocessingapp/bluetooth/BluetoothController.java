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
 * Обеспечивает подключение к сопряжённым устройствам и управление текущим соединением.
 */
public class BluetoothController {

    private static final String TAG = "BluetoothController";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static BluetoothController instance;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;

    /**
     * Приватный конструктор для реализации шаблона Singleton.
     */
    private BluetoothController() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Получить единственный экземпляр {@link BluetoothController}.
     *
     * @return экземпляр BluetoothController
     */
    public static BluetoothController getInstance() {
        if (instance == null) {
            instance = new BluetoothController();
        }
        return instance;
    }

    /**
     * Проверяет наличие разрешения BLUETOOTH_CONNECT.
     *
     * @param context контекст приложения
     * @return true, если разрешение предоставлено, иначе false
     */
    public boolean hasBluetoothPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }



    /**
     * Проверяет наличие разрешения BLUETOOTH_SCAN.
     *
     * @param context контекст приложения
     * @return true, если разрешение предоставлено, иначе false
     */
    public boolean hasScanPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Проверяет, поддерживает ли устройство Bluetooth.
     *
     * @return true, если Bluetooth поддерживается, иначе false
     */
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    /**
     * Проверяет, включён ли Bluetooth на устройстве.
     *
     * @return true, если Bluetooth включён, иначе false
     */
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Получает список сопряжённых Bluetooth-устройств.
     *
     * @param context контекст для проверки разрешений
     * @return список сопряжённых устройств или пустой список, если нет доступа
     */
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
     * Подключается к переданному Bluetooth-устройству.
     * Запускается в отдельном потоке.
     *
     * @param device   устройство, к которому нужно подключиться
     * @param callback колбэк для уведомления об успешном или неудачном подключении
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connectToDevice(BluetoothDevice device, ConnectionCallback callback) {
        disconnect(); // Закрываем предыдущее соединение, если есть

        new Thread(() -> {
            try {
                BluetoothSocket tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                tmp.connect();
                socket = tmp;
                connectedDevice = device;
                callback.onConnected();
            } catch (IOException e) {
                Log.e(TAG, "Ошибка подключения: " + e.getMessage());
                callback.onConnectionFailed(e);
            }
        }).start();
    }

    /**
     * Закрывает текущее соединение, если оно есть.
     * Выполняется в фоновом потоке.
     */
    public void disconnect() {
        new Thread(() -> {
            try {
                if (socket != null) {
                    socket.close();
                    Log.d(TAG, "Соединение закрыто");
                }
            } catch (IOException e) {
                Log.e(TAG, "Ошибка закрытия: " + e.getMessage());
            } finally {
                socket = null;
                connectedDevice = null;
            }
        }).start();
    }

    /**
     * Возвращает текущее подключённое устройство, если оно есть.
     *
     * @return {@link BluetoothDevice} или null, если нет соединения
     */
    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }

    /**
     * Возвращает активный Bluetooth-сокет.
     *
     * @return {@link BluetoothSocket} или null
     */
    public BluetoothSocket getSocket() {
        return socket;
    }

    /**
     * Интерфейс для уведомления о результатах подключения к Bluetooth-устройству.
     */
    public interface ConnectionCallback {
        /**
         * Вызывается при успешном подключении.
         */
        void onConnected();

        /**
         * Вызывается при неудаче подключения.
         *
         * @param e исключение с причиной ошибки
         */
        void onConnectionFailed(Exception e);
    }
}

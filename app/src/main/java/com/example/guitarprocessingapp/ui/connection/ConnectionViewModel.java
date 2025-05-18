package com.example.guitarprocessingapp.ui.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guitarprocessingapp.bluetooth.BluetoothController;

import java.util.ArrayList;
import java.util.List;

public class ConnectionViewModel extends ViewModel {

    private final MutableLiveData<List<BluetoothDevice>> pairedDevices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> selectedDeviceAddress = new MutableLiveData<>();
    private final MutableLiveData<String> connectionMessage = new MutableLiveData<>();

    private BluetoothDevice connectedDevice = null;

    private BroadcastReceiver bondedDevicesReceiver;
    private boolean isReceiverRegistered = false;

    public LiveData<List<BluetoothDevice>> getPairedDevices() {
        return pairedDevices;
    }

    public LiveData<String> getSelectedDeviceAddress() {
        return selectedDeviceAddress;
    }

    public LiveData<String> getConnectionMessage() {
        return connectionMessage;
    }

    /**
     * Инициализация: регистрируем ресивер для отслеживания изменений в списке сопряженных устройств.
     */
    public void startListeningPairedDevices(Context context) {
        if (isReceiverRegistered) return;

        bondedDevicesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    // При изменении состояния сопряжения обновляем список
                    loadPairedDevices(context);
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(bondedDevicesReceiver, filter);
        isReceiverRegistered = true;

        // Изначальная загрузка
        loadPairedDevices(context);
    }

    /**
     * Отписка от BroadcastReceiver.
     */
    public void stopListeningPairedDevices(Context context) {
        if (isReceiverRegistered) {
            context.unregisterReceiver(bondedDevicesReceiver);
            isReceiverRegistered = false;
        }
    }

    /**
     * Загружает список ранее сопряжённых устройств Bluetooth.
     */
    public void loadPairedDevices(Context context) {
        List<BluetoothDevice> devices = BluetoothController.getInstance().getPairedDevices(context);
        pairedDevices.postValue(devices);
    }

    /**
     * Обрабатывает выбор устройства пользователем.
     * Если устройство уже подключено — отключается.
     * Если другое устройство — подключается к нему.
     */
    public void onDeviceSelected(Context context, BluetoothDevice device) {
        if (device == null) return;

        String deviceAddress = device.getAddress();

        // Если выбрано уже подключенное устройство — отключаем
        if (connectedDevice != null && deviceAddress.equals(connectedDevice.getAddress())) {
            BluetoothController.getInstance().disconnect();
            connectionMessage.postValue("Отключено от устройства: " + device.getName());
            selectedDeviceAddress.postValue(null);
            connectedDevice = null;
            return;
        }

        // Подключаемся к новому устройству
        if (!BluetoothController.getInstance().hasBluetoothPermission(context)) {
            Log.w("ConnectionViewModel", "Нет разрешения на BLUETOOTH_CONNECT");
            connectionMessage.postValue("Нет разрешения на подключение Bluetooth");
            return;
        }

        BluetoothController.getInstance().connectToDevice(device, new BluetoothController.ConnectionCallback() {
            @Override
            public void onConnected() {
                connectedDevice = device;
                selectedDeviceAddress.postValue(deviceAddress);
                connectionMessage.postValue("Подключено к устройству: " + device.getName());
                Log.d("ConnectionViewModel", "Подключено к устройству: " + device.getName());
            }

            @Override
            public void onConnectionFailed(Exception e) {
                connectionMessage.postValue("Не удалось подключиться: " + e.getMessage());
                Log.e("ConnectionViewModel", "Не удалось подключиться: " + e.getMessage());
                // Сбрасываем выделение устройства в списке
                selectedDeviceAddress.postValue(null);
                connectedDevice = null;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Здесь должен быть вызов stopListeningPairedDevices,
        // но ViewModel не хранит Context — этот вызов нужно делать из Fragment при onDestroyView/onDestroy
    }
}

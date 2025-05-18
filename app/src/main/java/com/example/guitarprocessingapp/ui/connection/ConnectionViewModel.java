package com.example.guitarprocessingapp.ui.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guitarprocessingapp.bluetooth.BluetoothController;

import java.util.List;

public class ConnectionViewModel extends ViewModel {

    private final MutableLiveData<List<BluetoothDevice>> pairedDevices = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDeviceAddress = new MutableLiveData<>();

    public LiveData<List<BluetoothDevice>> getPairedDevices() {
        return pairedDevices;
    }

    public LiveData<String> getSelectedDeviceAddress() {
        return selectedDeviceAddress;
    }

    public void loadPairedDevices(Context context) {
        List<BluetoothDevice> devices = BluetoothController.getInstance().getPairedDevices(context);
        pairedDevices.setValue(devices);
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        if (device == null) return;
        selectedDeviceAddress.setValue(device.getAddress());

        if (!BluetoothController.getInstance().hasBluetoothPermission(context)) {
            Log.w("ConnectionViewModel", "Нет разрешения на BLUETOOTH_CONNECT");
            return;
        }

        BluetoothController.getInstance().connectToDevice(device, new BluetoothController.ConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d("ConnectionViewModel", "Подключено к устройству: " + device.getName());
            }

            @Override
            public void onConnectionFailed(Exception e) {
                Log.e("ConnectionViewModel", "Не удалось подключиться: " + e.getMessage());
            }
        });
    }
}

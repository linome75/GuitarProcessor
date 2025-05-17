package com.example.guitarprocessingapp.ui.connection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guitarprocessingapp.bluetooth.BluetoothManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        List<BluetoothDevice> devices = new ArrayList<>();

        if (bluetoothAdapter == null) {
            pairedDevices.setValue(devices);
            return;
        }

        if (!hasBluetoothConnectPermission(context)) {
            pairedDevices.setValue(devices);
            return;
        }

        try {
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            if (bondedDevices != null) {
                devices.addAll(bondedDevices);
            }
        } catch (SecurityException e) {
            Log.e("ConnectionViewModel", "Не удалось получить сопряжённые устройства: " + e.getMessage());
        }

        pairedDevices.setValue(devices);
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        if (device == null) return;

        selectedDeviceAddress.setValue(device.getAddress());

        if (!hasBluetoothConnectPermission(context)) {
            Log.w("ConnectionViewModel", "Нет разрешения на BLUETOOTH_CONNECT — соединение невозможно");
            return;
        }

        try {
            String name = device.getName();
            Log.d("ConnectionViewModel", "Подключение к: " + (name != null ? name : device.getAddress()));
        } catch (SecurityException e) {
            Log.e("ConnectionViewModel", "Нет доступа к имени устройства");
        }


        BluetoothManager.getInstance().connectToDevice(context, device, new BluetoothManager.ConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d("ConnectionViewModel", "Устройство успешно подключено");
            }

            @Override
            public void onConnectionFailed(Exception e) {
                Log.e("ConnectionViewModel", "Не удалось подключиться: " + e.getMessage());
            }
        });
    }



    private boolean hasBluetoothConnectPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED;
    }
}

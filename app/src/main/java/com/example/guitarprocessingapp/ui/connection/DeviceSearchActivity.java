package com.example.guitarprocessingapp.ui.connection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guitarprocessingapp.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceSearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private DeviceAdapter adapter;
    private final List<BluetoothDevice> foundDevices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private final Handler handler = new Handler();
    private final int REFRESH_INTERVAL = 5000;

    private final Runnable periodicDiscovery = new Runnable() {
        @Override
        public void run() {
            startDiscovery();
            handler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startDiscovery();
                    handler.postDelayed(periodicDiscovery, REFRESH_INTERVAL);
                }
            });

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !foundDevices.contains(device)) {
                    foundDevices.add(device);
                    updateList();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                updateList();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            finish(); // Bluetooth не поддерживается
        }

        recyclerView = findViewById(R.id.recyclerAvailableDevices);
        textEmpty = findViewById(R.id.textEmpty);
        adapter = new DeviceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Включаем стрелку "назад"
            getSupportActionBar().setTitle("Поиск устройств");      // Устанавливаем заголовок
        }


        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        checkPermissionAndStartDiscovery();
    }

    private void checkPermissionAndStartDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
                return;
            }
        }
        startDiscovery();
        handler.postDelayed(periodicDiscovery, REFRESH_INTERVAL);
    }

    private void startDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        foundDevices.clear();
        adapter.setDevices(foundDevices);
        bluetoothAdapter.startDiscovery();
    }

    private void updateList() {
        adapter.setDevices(foundDevices);
        textEmpty.setVisibility(foundDevices.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
        handler.removeCallbacks(periodicDiscovery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Назад
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            startDiscovery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

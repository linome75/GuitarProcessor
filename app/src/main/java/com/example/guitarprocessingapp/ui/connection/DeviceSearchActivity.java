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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_PAIR_DEVICE = 1002;

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
                Toast.makeText(DeviceSearchActivity.this, "Поиск устройств завершён", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                int prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);

                if (bondState == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(DeviceSearchActivity.this,
                            "Успешно сопряжено с " + device.getName(),
                            Toast.LENGTH_SHORT).show();
                    finish();  // Возвращаемся к предыдущему экрану
                } else if (bondState == BluetoothDevice.BOND_NONE && prevBondState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(DeviceSearchActivity.this,
                            "Не удалось сопрячь устройство " + device.getName(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startDiscoveryWithMessage();
                } else {
                    Toast.makeText(this, "Разрешение на сканирование не предоставлено", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Поиск устройств");
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth не поддерживается на этом устройстве", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        recyclerView = findViewById(R.id.recyclerAvailableDevices);
        textEmpty = findViewById(R.id.textEmpty);
        adapter = new DeviceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnDeviceSelectListener(device -> {
            if (!hasBluetoothPermission()) {
                Toast.makeText(this, "Нет разрешения на Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                // Запускаем сопряжение (вызывает стандартное диалоговое окно Android)
                device.createBond();
                Toast.makeText(this, "Начинается сопряжение с " + device.getName(), Toast.LENGTH_SHORT).show();
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Toast.makeText(this, "Устройство уже сопряжено: " + device.getName(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        checkPermissionAndStartDiscovery();
    }

    private boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void checkPermissionAndStartDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                return;
            }
        }
        startDiscoveryWithMessage();
    }

    private void startDiscoveryWithMessage() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        foundDevices.clear();
        adapter.setDevices(foundDevices);
        Toast.makeText(this, "Поиск устройств...", Toast.LENGTH_SHORT).show();
        bluetoothAdapter.startDiscovery();
    }

    private void updateList() {
        adapter.setDevices(foundDevices);
        textEmpty.setVisibility(foundDevices.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            Toast.makeText(this, "Обновление списка устройств...", Toast.LENGTH_SHORT).show();
            startDiscoveryWithMessage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startDiscoveryWithMessage();
            } else {
                Toast.makeText(this, "Bluetooth не включён", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

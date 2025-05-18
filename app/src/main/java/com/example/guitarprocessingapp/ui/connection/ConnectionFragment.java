package com.example.guitarprocessingapp.ui.connection;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.guitarprocessingapp.R;
import com.example.guitarprocessingapp.bluetooth.BluetoothController;
import com.example.guitarprocessingapp.databinding.FragmentConnectionBinding;

public class ConnectionFragment extends Fragment {

    private FragmentConnectionBinding binding;
    private ConnectionViewModel viewModel;
    private DeviceAdapter adapter;

    private final BluetoothController bluetoothController = BluetoothController.getInstance();

    private final ActivityResultLauncher<Intent> bluetoothEnableLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadDevices();
                } else {
                    Toast.makeText(requireContext(), "Bluetooth отключён", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkBluetoothEnabledAndLoad();
                } else {
                    Toast.makeText(requireContext(), "Разрешение не предоставлено", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConnectionBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ConnectionViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerView();

        adapter.setOnDeviceSelectListener(device ->
                viewModel.connectToDevice(requireContext(), device)
        );

        viewModel.getPairedDevices().observe(getViewLifecycleOwner(), devices -> {
            adapter.setDevices(devices);

            if (devices == null || devices.isEmpty()) {
                binding.recyclerDevices.setVisibility(View.GONE);
                binding.textEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerDevices.setVisibility(View.VISIBLE);
                binding.textEmpty.setVisibility(View.GONE);
            }
        });

        viewModel.getSelectedDeviceAddress().observe(getViewLifecycleOwner(), adapter::setSelectedAddress);

        checkPermissionsAndLoadDevices();
    }

    private void setupRecyclerView() {
        adapter = new DeviceAdapter();
        binding.recyclerDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDevices.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.recycler_divider);
        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }
        binding.recyclerDevices.addItemDecoration(divider);
    }

    private void checkPermissionsAndLoadDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!bluetoothController.hasBluetoothPermission(requireContext())) {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                return;
            }
        }
        checkBluetoothEnabledAndLoad();
    }

    private void checkBluetoothEnabledAndLoad() {
        if (!bluetoothController.isBluetoothSupported()) {
            Toast.makeText(requireContext(), "Bluetooth не поддерживается на этом устройстве", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        if (!bluetoothController.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        } else {
            loadDevices();
        }
    }

    private void loadDevices() {
        viewModel.loadPairedDevices(requireContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

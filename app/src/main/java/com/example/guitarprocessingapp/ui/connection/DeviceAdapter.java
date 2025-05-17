package com.example.guitarprocessingapp.ui.connection;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guitarprocessingapp.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface OnDeviceSelectListener {
        void onDeviceSelected(BluetoothDevice device);
    }

    private final List<BluetoothDevice> devices = new ArrayList<>();
    private String selectedAddress = null;
    private OnDeviceSelectListener listener;

    public void setDevices(List<BluetoothDevice> newDevices) {
        devices.clear();
        devices.addAll(newDevices);
        notifyDataSetChanged();
    }

    public void setSelectedAddress(String address) {
        this.selectedAddress = address;
        notifyDataSetChanged();
    }

    public void setOnDeviceSelectListener(OnDeviceSelectListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        Context context = holder.itemView.getContext();

        String displayName = "Unknown device";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
            String name = device.getName();
            displayName = (name != null && !name.isEmpty()) ? name : device.getAddress();
        }

        holder.name.setText(displayName);

        boolean isSelected = device.getAddress().equals(selectedAddress);
        holder.radioButton.setChecked(isSelected);

        holder.radioButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceSelected(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        RadioButton radioButton;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.device_name);
            radioButton = itemView.findViewById(R.id.device_radio);
        }
    }
}

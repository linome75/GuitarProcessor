package com.example.guitarprocessingapp.ui.connection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.guitarprocessingapp.databinding.FragmentConnectionBinding;

public class ConnectionFragment extends Fragment {

    private FragmentConnectionBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConnectionViewModel connectionViewModel =
                new ViewModelProvider(this).get(ConnectionViewModel.class);

        binding = FragmentConnectionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textConnection;
        connectionViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
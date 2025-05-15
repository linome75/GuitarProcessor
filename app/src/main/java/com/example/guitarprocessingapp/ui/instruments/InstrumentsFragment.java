package com.example.guitarprocessingapp.ui.instruments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.guitarprocessingapp.databinding.FragmentInstrumentsBinding;

public class InstrumentsFragment extends Fragment {

    private FragmentInstrumentsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InstrumentsViewModel instrumentsViewModel =
                new ViewModelProvider(this).get(InstrumentsViewModel.class);

        binding = FragmentInstrumentsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textInstruments;
        instrumentsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
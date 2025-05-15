package com.example.guitarprocessingapp.ui.effects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.guitarprocessingapp.databinding.FragmentEffectsBinding;

public class EffectsFragment extends Fragment {

    private FragmentEffectsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EffectsViewModel effectsViewModel =
                new ViewModelProvider(this).get(EffectsViewModel.class);

        binding = FragmentEffectsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textEffects;
        effectsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
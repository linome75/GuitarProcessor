package com.example.guitarprocessingapp.ui.effects;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guitarprocessingapp.R;
import com.example.guitarprocessingapp.databinding.FragmentEffectsBinding;

public class EffectsFragment extends Fragment {

    private FragmentEffectsBinding binding;
    private EffectsViewModel viewModel;
    private EffectAdapter adapter;

    // Флаг для предотвращения многократного быстрого запуска активности
    private boolean isLaunchingSettings = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEffectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EffectsViewModel.class);

        adapter = new EffectAdapter();
        binding.recyclerEffects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerEffects.setAdapter(adapter);

        // Добавляем разделитель между элементами списка
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.recycler_divider);
        if (dividerDrawable != null) {
            divider.setDrawable(dividerDrawable);
        }
        binding.recyclerEffects.addItemDecoration(divider);

        adapter.setOnEffectSelectListener(position -> viewModel.selectEffect(position));

        adapter.setOnEffectNameClickListener(position -> {
            if (isLaunchingSettings) return; // Игнорируем повторные клики

            isLaunchingSettings = true;

            Intent intent = new Intent(requireContext(), EffectSettingsActivity.class);
            intent.putExtra(EffectSettingsActivity.EXTRA_EFFECT_INDEX, position);
            startActivity(intent);

            // Сброс флага через короткую задержку, чтобы исключить повторные нажатия
            binding.recyclerEffects.postDelayed(() -> isLaunchingSettings = false, 300);
        });

        viewModel.getEffects().observe(getViewLifecycleOwner(), effects -> {
            adapter.setEffects(effects);

            if (effects == null || effects.isEmpty()) {
                binding.recyclerEffects.setVisibility(View.GONE);
                binding.textEmptyEffects.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerEffects.setVisibility(View.VISIBLE);
                binding.textEmptyEffects.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.guitarprocessingapp.ui.effects;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.guitarprocessingapp.bluetooth.BluetoothController;
import com.example.guitarprocessingapp.databinding.FragmentEffectsBinding;

import java.util.List;

public class EffectsFragment extends Fragment {

    private FragmentEffectsBinding binding;
    private EffectsViewModel viewModel;
    private EffectAdapter adapter;
    private boolean isLaunchingSettings = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEffectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Включаем меню в фрагменте
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BluetoothController.getInstance().initialize(requireContext());

        viewModel = new ViewModelProvider(this).get(EffectsViewModel.class);
        adapter = new EffectAdapter();

        setupRecyclerView();
        observeViewModel();

        viewModel.loadEffects();
    }

    private void setupRecyclerView() {
        binding.recyclerEffects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerEffects.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.recycler_divider);
        if (drawable != null) divider.setDrawable(drawable);
        binding.recyclerEffects.addItemDecoration(divider);

        adapter.setOnEffectSelectListener(position -> viewModel.selectEffect(position));
        adapter.setOnEffectNameClickListener(position -> {
            if (isLaunchingSettings) return;
            isLaunchingSettings = true;
            Intent intent = new Intent(requireContext(), EffectSettingsActivity.class);
            intent.putExtra(EffectSettingsActivity.EXTRA_EFFECT_INDEX, position);
            startActivity(intent);
            binding.recyclerEffects.postDelayed(() -> isLaunchingSettings = false, 300);
        });
    }

    private void observeViewModel() {
        viewModel.getEffects().observe(getViewLifecycleOwner(), this::updateEffectsList);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showErrorMessage);
    }

    private void updateEffectsList(List<EffectItem> effects) {
        boolean isEmpty = effects == null || effects.isEmpty();
        adapter.setEffects(effects);
        binding.recyclerEffects.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.textEmptyEffects.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showErrorMessage(String message) {
        adapter.setEffects(null);
        binding.recyclerEffects.setVisibility(View.GONE);
        binding.textEmptyEffects.setText(message);
        binding.textEmptyEffects.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_reload) {
            reloadEffectsList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadEffectsList() {
        // Очистить список и ошибки
        adapter.setEffects(null);
        binding.recyclerEffects.setVisibility(View.GONE);
        binding.textEmptyEffects.setVisibility(View.GONE);
        binding.textEmptyEffects.setText("");

        // Загрузить эффекты заново
        viewModel.loadEffects();
    }
}

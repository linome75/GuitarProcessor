package com.example.guitarprocessingapp.ui.instruments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guitarprocessingapp.databinding.FragmentInstrumentsBinding;

import java.util.List;

public class InstrumentsFragment extends Fragment {

    private FragmentInstrumentsBinding binding;
    private InstrumentsViewModel viewModel;
    private InstrumentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInstrumentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InstrumentsViewModel.class);

        adapter = new InstrumentAdapter();
        binding.recyclerInstruments.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Добавляем разделители между элементами
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        binding.recyclerInstruments.addItemDecoration(divider);

        binding.recyclerInstruments.setAdapter(adapter);

        // Наблюдаем за списком инструментов
        viewModel.getInstrumentList().observe(getViewLifecycleOwner(), instruments -> {
            adapter.setInstruments(instruments);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

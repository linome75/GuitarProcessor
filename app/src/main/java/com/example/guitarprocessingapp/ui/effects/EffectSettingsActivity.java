package com.example.guitarprocessingapp.ui.effects;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guitarprocessingapp.R;
import com.example.guitarprocessingapp.databinding.ActivityEffectSettingsBinding;

import java.util.ArrayList;
import java.util.List;

public class EffectSettingsActivity extends AppCompatActivity {

    public static final String EXTRA_EFFECT_INDEX = "effect_index";

    private ActivityEffectSettingsBinding binding;
    private EffectItem originalEffect;
    private List<EffectParameter> parameterDraft; // Копия параметров
    private ParameterAdapter adapter;
    private int effectIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEffectSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        effectIndex = getIntent().getIntExtra(EXTRA_EFFECT_INDEX, -1);
        originalEffect = EffectsViewModel.getEffectByIndex(effectIndex);

        if (originalEffect == null) {
            finish(); // Ошибка: индекс невалидный
            return;
        }

        setTitle(originalEffect.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Создаем копию параметров, чтобы не менять оригинал сразу
        parameterDraft = new ArrayList<>();
        for (EffectParameter param : originalEffect.getParameters()) {
            parameterDraft.add(new EffectParameter(
                    param.getName(),
                    param.getMinValue(),
                    param.getMaxValue(),
                    param.getCurrentValue()
            ));
        }

        adapter = new ParameterAdapter(parameterDraft);
        binding.recyclerParams.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerParams.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_effect_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Назад — ничего не сохраняем
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            // Сохраняем изменения в оригинальный EffectItem
            originalEffect.setParameters(parameterDraft);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

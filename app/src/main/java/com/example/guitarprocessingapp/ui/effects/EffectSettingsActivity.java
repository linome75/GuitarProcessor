package com.example.guitarprocessingapp.ui.effects;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guitarprocessingapp.R;
import com.example.guitarprocessingapp.databinding.ActivityEffectSettingsBinding;

import java.util.ArrayList;
import java.util.List;

public class EffectSettingsActivity extends AppCompatActivity {

    public static final String EXTRA_EFFECT_INDEX = "effect_index";

    private ActivityEffectSettingsBinding binding;
    private EffectItem originalEffect;
    private List<EffectParameter> parameterDraft;
    private ParameterAdapter adapter;
    private int effectIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEffectSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Настройка Toolbar как ActionBar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Получение и проверка индекса эффекта
        effectIndex = getIntent().getIntExtra(EXTRA_EFFECT_INDEX, -1);
        originalEffect = EffectsViewModel.getEffectByIndex(effectIndex);

        if (originalEffect == null) {
            finish(); // Недопустимый индекс — закрываем активность
            return;
        }

        // Установка названия эффекта как заголовка
        getSupportActionBar().setTitle(originalEffect.getName());

        // Создание копии параметров для редактирования
        parameterDraft = new ArrayList<>();
        for (EffectParameter param : originalEffect.getParameters()) {
            parameterDraft.add(new EffectParameter(
                    param.getName(),
                    param.getMinValue(),
                    param.getMaxValue(),
                    param.getCurrentValue(),
                    param.getUnit()
            ));
        }

        // Настройка RecyclerView
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
            finish(); // Назад — выход без сохранения
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            // Сохраняем изменения и выходим
            originalEffect.setParameters(parameterDraft);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.example.guitarprocessingapp.ui.effects;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guitarprocessingapp.R;
import com.example.guitarprocessingapp.bluetooth.BluetoothController;
import com.example.guitarprocessingapp.databinding.ActivityEffectSettingsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        effectIndex = getIntent().getIntExtra(EXTRA_EFFECT_INDEX, -1);
        originalEffect = EffectsViewModel.getEffectByIndex(effectIndex);

        if (originalEffect == null) {
            finish();
            return;
        }

        getSupportActionBar().setTitle(originalEffect.getName());

        parameterDraft = new ArrayList<>();
        adapter = new ParameterAdapter(parameterDraft);
        binding.recyclerParams.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerParams.setAdapter(adapter);

        fetchEffectParameters(originalEffect.getName());
    }

    private void fetchEffectParameters(String effectName) {
        String command = "GET_PARAMS:" + effectName;
        BluetoothController.getInstance().sendCommand(command, response -> {
            if (response == null || response.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_failed_to_get_params, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            List<EffectParameter> params = parseParametersFromJson(response);
            if (params.isEmpty()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_parsing_params, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            runOnUiThread(() -> {
                originalEffect.setParameters(params);
                parameterDraft.clear();
                parameterDraft.addAll(params);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private List<EffectParameter> parseParametersFromJson(String json) {
        List<EffectParameter> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject paramObj = array.getJSONObject(i);
                String name = paramObj.optString("name", "Param" + i);
                int min = paramObj.optInt("min", 0);
                int max = paramObj.optInt("max", 100);
                int current = paramObj.optInt("value", 0);
                String unit = paramObj.optString("unit", null);
                result.add(new EffectParameter(name, min, max, current, unit));
            }
        } catch (JSONException e) {
            Log.e("EffectSettingsActivity", "JSON parsing error", e);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_effect_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            originalEffect.setParameters(parameterDraft);

            BluetoothController.getInstance().sendEffectParamsIndividually(
                    originalEffect.getName(),
                    parameterDraft,
                    response -> runOnUiThread(() -> {
                        if (response == null || !response.equals("OK")) {
                            Toast.makeText(this, R.string.error_updating_params, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.params_updated_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
            );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

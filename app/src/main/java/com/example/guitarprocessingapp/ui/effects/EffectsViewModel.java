package com.example.guitarprocessingapp.ui.effects;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guitarprocessingapp.R;
import com.example.guitarprocessingapp.bluetooth.BluetoothController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EffectsViewModel extends ViewModel {

    private final MutableLiveData<List<EffectItem>> effects = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private int selectedPosition = -1;
    private static EffectsViewModel instance;

    private final BluetoothController controller = BluetoothController.getInstance();

    public EffectsViewModel() {
        instance = this;
    }

    public void loadEffects() {
        if (controller.getConnectedDevice() == null) {
            errorMessage.postValue(getStringResource(R.string.error_device_not_connected));
            effects.postValue(new ArrayList<>());
            return;
        }

        controller.validateGuitarProcessor(isValid -> {
            if (isValid) {
                requestEffectsFromDevice();
            } else {
                errorMessage.postValue(getStringResource(R.string.error_not_guitar_processor));
                effects.postValue(new ArrayList<>());
            }
        });
    }

    private void requestEffectsFromDevice() {
        String cmdGetEffects = getStringResource(R.string.cmd_get_effects);
        controller.sendCommand(cmdGetEffects, response -> {
            if (response == null || response.isEmpty()) {
                errorMessage.postValue(getStringResource(R.string.error_failed_to_get_effects));
                effects.postValue(new ArrayList<>());
                return;
            }

            List<EffectItem> parsedEffects = parseEffectsJson(response);
            if (parsedEffects.isEmpty()) {
                errorMessage.postValue(getStringResource(R.string.error_empty_or_unrecognized_effects));
            }
            effects.postValue(parsedEffects);
        });
    }

    private String getStringResource(int resId) {
        Context context = controller.getAppContext();
        if (context == null) return "";
        return context.getString(resId);
    }

    public LiveData<List<EffectItem>> getEffects() {
        return effects;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void selectEffect(int position) {
        List<EffectItem> current = effects.getValue();
        if (current == null || position < 0 || position >= current.size()) return;

        List<EffectItem> updated = new ArrayList<>(current);

        if (selectedPosition == position) {
            EffectItem effect = updated.get(position);
            effect.setSelected(false);
            sendEffectCommand(effect.getName(), false);
            selectedPosition = -1;
        } else {
            if (selectedPosition >= 0 && selectedPosition < updated.size()) {
                EffectItem prevEffect = updated.get(selectedPosition);
                prevEffect.setSelected(false);
                sendEffectCommand(prevEffect.getName(), false);
            }

            EffectItem newEffect = updated.get(position);
            newEffect.setSelected(true);
            sendEffectCommand(newEffect.getName(), true);
            selectedPosition = position;
        }

        effects.setValue(updated);
    }

    private void sendEffectCommand(String effectName, boolean enable) {
        if (controller.getSocket() == null || !controller.getSocket().isConnected()) {
            errorMessage.postValue(getStringResource(R.string.error_device_not_connected));
            return;
        }

        controller.sendEffectCommand(effectName, enable, response -> {
            if (response == null) {
                String errMsg = String.format(getStringResource(R.string.error_send_command_failed), effectName);
                errorMessage.postValue(errMsg);
            }
        });
    }

    private List<EffectItem> parseEffectsJson(String json) {
        List<EffectItem> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject effectObj = array.getJSONObject(i);
                String name = effectObj.optString("name", "Unknown");
                boolean enabled = effectObj.optBoolean("enabled", false);
                EffectItem effect = new EffectItem(name, enabled);
                list.add(effect);
            }
        } catch (JSONException e) {
            Log.e("EffectsViewModel", "Ошибка парсинга JSON эффектов", e);
        }
        return list;
    }

    public static EffectItem getEffectByIndex(int index) {
        if (instance == null || instance.effects.getValue() == null) return null;
        List<EffectItem> list = instance.effects.getValue();
        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
    }
}

package com.example.guitarprocessingapp.ui.effects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class EffectSettingsViewModel extends ViewModel {

    private final MutableLiveData<List<EffectParameter>> parameters = new MutableLiveData<>();
    private EffectItem currentEffect;

    public void setEffect(EffectItem effect) {
        this.currentEffect = effect;
        parameters.setValue(effect.getParameters());
    }

    public void saveChanges() {
        if (currentEffect != null && parameters.getValue() != null) {
            currentEffect.setParameters(parameters.getValue());
        }
    }

    public LiveData<List<EffectParameter>> getParameters() {
        return parameters;
    }
}

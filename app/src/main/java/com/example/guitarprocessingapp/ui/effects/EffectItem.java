package com.example.guitarprocessingapp.ui.effects;

import java.util.ArrayList;
import java.util.List;

public class EffectItem {
    private String name;
    private boolean enabled;
    private List<EffectParameter> parameters;

    public EffectItem(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
        this.parameters = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return enabled;
    }

    public void setSelected(boolean enabled) {
        this.enabled = enabled;
    }

    public List<EffectParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<EffectParameter> newParams) {
        this.parameters = new ArrayList<>();
        for (EffectParameter p : newParams) {
            this.parameters.add(new EffectParameter(
                    p.getName(), p.getMinValue(), p.getMaxValue(), p.getCurrentValue()
            ));
        }
    }
}

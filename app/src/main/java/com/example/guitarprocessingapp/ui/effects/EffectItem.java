package com.example.guitarprocessingapp.ui.effects;

import java.util.ArrayList;
import java.util.List;

public class EffectItem {
    private String name;
    private boolean enabled;   // включен или выключен эффект
    private boolean selected;  // выбран в UI
    private List<EffectParameter> parameters;

    public EffectItem(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
        this.selected = false;
        this.parameters = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<EffectParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<EffectParameter> newParams) {
        this.parameters = new ArrayList<>();
        for (EffectParameter p : newParams) {
            this.parameters.add(new EffectParameter(
                    p.getName(), p.getMinValue(), p.getMaxValue(), p.getCurrentValue(), p.getUnit()
            ));
        }
    }
}

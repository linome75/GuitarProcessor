package com.example.guitarprocessingapp.ui.effects;

public class EffectParameter {
    private final String name;
    private final int minValue;
    private final int maxValue;
    private int currentValue;

    public EffectParameter(String name, int minValue, int maxValue, int currentValue) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
    }

    public String getName() {
        return name;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int value) {
        this.currentValue = value;
    }
}

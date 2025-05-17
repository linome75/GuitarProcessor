package com.example.guitarprocessingapp.ui.effects;

public class EffectParameter {

    private String name;
    private int minValue;
    private int maxValue;
    private int currentValue;
    private String unit;

    public EffectParameter(String name, int minValue, int maxValue, int currentValue, String unit) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
        this.unit = unit;
    }

    public EffectParameter(String name, int minValue, int maxValue, int currentValue) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
        this.unit = null;
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

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
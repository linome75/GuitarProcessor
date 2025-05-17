package com.example.guitarprocessingapp.ui.instruments;

public class Instrument {
    private final String name;
    private final int iconResId;

    public Instrument(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}

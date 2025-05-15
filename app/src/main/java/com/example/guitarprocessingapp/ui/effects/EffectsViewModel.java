package com.example.guitarprocessingapp.ui.effects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EffectsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EffectsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
package com.example.guitarprocessingapp.ui.instruments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InstrumentsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public InstrumentsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
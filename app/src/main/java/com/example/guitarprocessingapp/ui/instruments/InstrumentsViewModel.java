package com.example.guitarprocessingapp.ui.instruments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guitarprocessingapp.R;

import java.util.ArrayList;
import java.util.List;

public class InstrumentsViewModel extends ViewModel {

    private final MutableLiveData<List<Instrument>> instrumentList = new MutableLiveData<>();

    public InstrumentsViewModel() {
        loadInstruments();
    }

    private void loadInstruments() {
        // Заглушка — список инструментов с иконками
        List<Instrument> list = new ArrayList<>();
        list.add(new Instrument("Метроном", R.drawable.ic_methronome)); // R.drawable.ic_guitar — твоя иконка
        list.add(new Instrument("Тюнер", R.drawable.ic_tuner));
        // Добавь нужные инструменты

        instrumentList.setValue(list);
    }

    public LiveData<List<Instrument>> getInstrumentList() {
        return instrumentList;
    }
}

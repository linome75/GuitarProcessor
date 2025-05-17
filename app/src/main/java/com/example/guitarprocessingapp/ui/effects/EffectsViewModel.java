package com.example.guitarprocessingapp.ui.effects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectsViewModel extends ViewModel {

    private final MutableLiveData<List<EffectItem>> effects = new MutableLiveData<>();
    private int selectedPosition = -1; // Нет выбранного эффекта по умолчанию

    // Статическая ссылка на инстанс для доступа из Activity (можно заменить на более чистую архитектуру)
    private static EffectsViewModel instance;

    public EffectsViewModel() {
        instance = this;
        loadEffects();
    }

    private void loadEffects() {
        // Инициализация списка эффектов с параметрами (заглушка, потом можно получить по Bluetooth)
        List<EffectItem> list = new ArrayList<>();

        EffectItem overdrive = new EffectItem("Overdrive", false);
        overdrive.setParameters(Arrays.asList(
                new EffectParameter("Gain", 0, 100, 50, "%"),
                new EffectParameter("Tone", 0, 10, 5),
                new EffectParameter("Level", 0, 100, 75)
        ));

        EffectItem distortion = new EffectItem("Distortion", false);
        distortion.setParameters(Arrays.asList(
                new EffectParameter("Drive", 0, 100, 60),
                new EffectParameter("Tone", 0, 10, 7),
                new EffectParameter("Level", 0, 100, 80)
        ));

        EffectItem chorus = new EffectItem("Chorus", false);
        chorus.setParameters(Arrays.asList(
                new EffectParameter("Depth", 0, 100, 40),
                new EffectParameter("Rate", 0, 10, 5),
                new EffectParameter("Mix", 0, 100, 60)
        ));

        EffectItem delay = new EffectItem("Delay", false);
        delay.setParameters(Arrays.asList(
                new EffectParameter("Time", 0, 1000, 300),
                new EffectParameter("Feedback", 0, 100, 50),
                new EffectParameter("Mix", 0, 100, 70)
        ));

        EffectItem reverb = new EffectItem("Reverb", false);
        reverb.setParameters(Arrays.asList(
                new EffectParameter("Room Size", 0, 100, 80),
                new EffectParameter("Damping", 0, 100, 50),
                new EffectParameter("Mix", 0, 100, 60)
        ));

        list.add(overdrive);
        list.add(distortion);
        list.add(chorus);
        list.add(delay);
        list.add(reverb);

        effects.setValue(list);
    }

    public LiveData<List<EffectItem>> getEffects() {
        return effects;
    }

    public void selectEffect(int position) {
        if (effects.getValue() == null) return;

        List<EffectItem> list = new ArrayList<>(effects.getValue());

        if (selectedPosition == position) {
            // Повторное нажатие — сбрасываем выбор
            list.get(position).setSelected(false);
            selectedPosition = -1;
        } else {
            // Сбросить прошлый выбор, если был
            if (selectedPosition != -1 && selectedPosition < list.size()) {
                list.get(selectedPosition).setSelected(false);
            }
            // Установить новый выбор
            list.get(position).setSelected(true);
            selectedPosition = position;
        }

        effects.setValue(list);
    }

    /**
     * Получить эффект по индексу из текущего списка.
     * Возвращает null, если индекс невалидный или список не загружен.
     */
    public static EffectItem getEffectByIndex(int index) {
        if (instance == null || instance.effects.getValue() == null) return null;

        List<EffectItem> list = instance.effects.getValue();
        if (index < 0 || index >= list.size()) return null;

        return list.get(index);
    }
}

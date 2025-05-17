package com.example.guitarprocessingapp.ui.effects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guitarprocessingapp.R;

import java.util.ArrayList;
import java.util.List;

public class EffectAdapter extends RecyclerView.Adapter<EffectAdapter.EffectViewHolder> {

    private List<EffectItem> effects = new ArrayList<>();
    private OnEffectSelectListener onEffectSelectListener;
    private OnEffectNameClickListener onEffectNameClickListener;

    @NonNull
    @Override
    public EffectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_effect, parent, false);
        return new EffectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EffectViewHolder holder, int position) {
        EffectItem effect = effects.get(position);
        holder.name.setText(effect.getName());
        holder.radioButton.setChecked(effect.isSelected());

        // Нажатие **только на радиобаттон** выбирает эффект
        holder.radioButton.setOnClickListener(v -> {
            if (onEffectSelectListener != null) {
                onEffectSelectListener.onEffectSelected(position);
            }
        });

        // Нажатие **только на название** вызывает отдельный обработчик
        holder.name.setOnClickListener(v -> {
            if (onEffectNameClickListener != null) {
                onEffectNameClickListener.onEffectNameClicked(position);
            }
        });

        // Чтобы клик по элементу (кроме радиобаттона и названия) не мешал,
        // мы НЕ ставим слушатель на itemView
    }

    @Override
    public int getItemCount() {
        return effects.size();
    }

    public void setEffects(List<EffectItem> newEffects) {
        effects = newEffects;
        notifyDataSetChanged();
    }

    public void setOnEffectSelectListener(OnEffectSelectListener listener) {
        this.onEffectSelectListener = listener;
    }

    public void setOnEffectNameClickListener(OnEffectNameClickListener listener) {
        this.onEffectNameClickListener = listener;
    }

    public interface OnEffectSelectListener {
        void onEffectSelected(int position);
    }

    public interface OnEffectNameClickListener {
        void onEffectNameClicked(int position);
    }

    static class EffectViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        RadioButton radioButton;

        public EffectViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.effect_name);
            radioButton = itemView.findViewById(R.id.effect_radiobutton);
        }
    }
}

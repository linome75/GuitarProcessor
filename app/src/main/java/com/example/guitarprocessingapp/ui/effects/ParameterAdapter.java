package com.example.guitarprocessingapp.ui.effects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guitarprocessingapp.R;

import java.util.List;

public class ParameterAdapter extends RecyclerView.Adapter<ParameterAdapter.ParameterViewHolder> {

    private final List<EffectParameter> parameters;

    public ParameterAdapter(List<EffectParameter> parameters) {
        this.parameters = parameters;
    }

    @NonNull
    @Override
    public ParameterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parameter, parent, false);
        return new ParameterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParameterViewHolder holder, int position) {
        EffectParameter param = parameters.get(position);
        holder.name.setText(param.getName() + (param.getUnit() == null ? "" : (", " + param.getUnit())));
        holder.value.setText(String.valueOf(param.getCurrentValue()));
        holder.min.setText(String.valueOf(param.getMinValue()));
        holder.max.setText(String.valueOf(param.getMaxValue()));

        holder.seekBar.setMax(param.getMaxValue() - param.getMinValue());
        holder.seekBar.setProgress(param.getCurrentValue() - param.getMinValue());

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int actualValue = progress + param.getMinValue();
                param.setCurrentValue(actualValue);
                holder.value.setText(String.valueOf(actualValue));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public int getItemCount() {
        return parameters.size();
    }

    static class ParameterViewHolder extends RecyclerView.ViewHolder {
        TextView name, value, min, max;
        SeekBar seekBar;

        public ParameterViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.param_name);
            value = itemView.findViewById(R.id.param_value);
            min = itemView.findViewById(R.id.param_min);
            max = itemView.findViewById(R.id.param_max);
            seekBar = itemView.findViewById(R.id.param_seekbar);
        }
    }
}

package com.example.guitarprocessingapp.ui.instruments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guitarprocessingapp.databinding.ItemInstrumentBinding;

import java.util.ArrayList;
import java.util.List;

public class InstrumentAdapter extends RecyclerView.Adapter<InstrumentAdapter.InstrumentViewHolder> {

    private List<Instrument> instruments = new ArrayList<>();
    private OnInstrumentClickListener clickListener;

    public interface OnInstrumentClickListener {
        void onInstrumentClick(int position);
    }

    public void setOnInstrumentClickListener(OnInstrumentClickListener listener) {
        this.clickListener = listener;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InstrumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemInstrumentBinding binding = ItemInstrumentBinding.inflate(inflater, parent, false);
        return new InstrumentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InstrumentViewHolder holder, int position) {
        Instrument instrument = instruments.get(position);
        holder.binding.instrumentName.setText(instrument.getName());
        holder.binding.instrumentIcon.setImageResource(instrument.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onInstrumentClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return instruments.size();
    }

    static class InstrumentViewHolder extends RecyclerView.ViewHolder {
        final ItemInstrumentBinding binding;

        public InstrumentViewHolder(ItemInstrumentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

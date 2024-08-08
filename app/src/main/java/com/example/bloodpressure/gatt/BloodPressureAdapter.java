package com.example.bloodpressure.gatt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodpressure.R;

import java.util.List;

public class BloodPressureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<BloodPressureReading> readings;

    public BloodPressureAdapter(List<BloodPressureReading> readings) {
        this.readings = readings;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_pressure_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_pressure, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            BloodPressureReading reading = readings.get(position - 1); // -1 because position 0 is header
            ((ItemViewHolder) holder).systolicTextView.setText(String.valueOf(reading.getSystolic()));
            ((ItemViewHolder) holder).diastolicTextView.setText(String.valueOf(reading.getDiastolic()));
            ((ItemViewHolder) holder).pulseTextView.setText(String.valueOf(reading.getPulse()));
        }
    }

    @Override
    public int getItemCount() {
        return readings.size() + 1;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView systolicTextView;
        TextView diastolicTextView;
        TextView pulseTextView;

        ItemViewHolder(View itemView) {
            super(itemView);
            systolicTextView = itemView.findViewById(R.id.systolic);
            diastolicTextView = itemView.findViewById(R.id.diastolic);
            pulseTextView = itemView.findViewById(R.id.pulse);
        }
    }
}

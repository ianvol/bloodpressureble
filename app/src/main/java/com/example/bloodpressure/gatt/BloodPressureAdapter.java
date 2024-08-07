package com.example.bloodpressure.gatt;// BloodPressureAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodpressure.R;

import java.util.List;

public class BloodPressureAdapter extends RecyclerView.Adapter<BloodPressureAdapter.ViewHolder> {

    private final List<BloodPressureReading> readings;

    public BloodPressureAdapter(List<BloodPressureReading> readings) {
        this.readings = readings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_pressure, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodPressureReading reading = readings.get(position);
        holder.systolicTextView.setText(String.valueOf(reading.getSystolic()));
        holder.diastolicTextView.setText(String.valueOf(reading.getDiastolic()));
        holder.pulseTextView.setText(String.valueOf(reading.getPulse()));
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView systolicTextView;
        TextView diastolicTextView;
        TextView pulseTextView;

        ViewHolder(View itemView) {
            super(itemView);
            systolicTextView = itemView.findViewById(R.id.systolic);
            diastolicTextView = itemView.findViewById(R.id.diastolic);
            pulseTextView = itemView.findViewById(R.id.pulse);
        }
    }
}

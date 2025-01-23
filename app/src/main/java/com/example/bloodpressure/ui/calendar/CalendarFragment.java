package com.example.bloodpressure.ui.calendar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodpressure.db.BloodPressureDao;
import com.example.bloodpressure.gatt.BloodPressureAdapter;
import com.example.bloodpressure.gatt.BloodPressureReading;
import com.example.bloodpressure.gatt.BluetoothLeService;
import com.example.bloodpressure.R;
import com.example.bloodpressure.databinding.FragmentHomeBinding;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    private BloodPressureAdapter adapter;
    private final List<BloodPressureReading> readings = new ArrayList<>();

    private BloodPressureDao bloodPressureDao;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        CalendarView calendarView = root.findViewById(R.id.calendarView);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BloodPressureAdapter(readings);
        recyclerView.setAdapter(adapter);

        bloodPressureDao = new BloodPressureDao(getActivity());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            int correctedMonth = month + 1; // correct index
            @SuppressLint("DefaultLocale") String selectedDate = String.format("%04d-%02d-%02d", year, correctedMonth, dayOfMonth);
            loadReadingsData(selectedDate);
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadReadingsData(@Nullable String selectedDate) {
        new Thread(() -> {
            List<BloodPressureReading> filteredReadings = new ArrayList<>();

            if (selectedDate != null && !selectedDate.isEmpty()) {
                filteredReadings = bloodPressureDao.getReadingsCertainDate(selectedDate);
            }

            List<BloodPressureReading> finalFilteredReadings = filteredReadings;
            requireActivity().runOnUiThread(() -> {
                readings.clear();
                readings.addAll(finalFilteredReadings);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}

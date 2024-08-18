package com.example.bloodpressure.ui.dashboard;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bloodpressure.R;
import com.example.bloodpressure.databinding.FragmentDashboardBinding;
import com.example.bloodpressure.db.BloodPressureDao;
import com.example.bloodpressure.gatt.BloodPressureAdapter;
import com.example.bloodpressure.gatt.BloodPressureReading;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    private static final String TAG = "DashboardFragment";
    private LineChart lineChart;
    private BloodPressureAdapter adapter;
    private final List<BloodPressureReading> readings = new ArrayList<>();

    private BloodPressureDao bloodPressureDao;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BloodPressureDao dao = new BloodPressureDao(getActivity());
        DashboardViewModelFactory factory = new DashboardViewModelFactory(dao);
        new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadAverageReading();

        adapter = new BloodPressureAdapter(readings);

        bloodPressureDao = new BloodPressureDao(getActivity());

        // Load previously saved readings from the database
        new Thread(() -> {
            List<BloodPressureReading> savedReadings = bloodPressureDao.getLast8Readings();
            requireActivity().runOnUiThread(() -> {
                readings.clear(); // Clear before loading - avoid duplication
                readings.addAll(savedReadings);
                adapter.notifyDataSetChanged();
                updateChart();
            });
        }).start();

        lineChart = root.findViewById(R.id.line_chart);
        setupChart();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("DefaultLocale")
    private void loadAverageReading() {
        BloodPressureDao dao = new BloodPressureDao(getActivity());
        List<BloodPressureReading> last8Readings = dao.getLast8Readings();
        dao.close();

        float totalSystolic = 0;
        float totalDiastolic = 0;
        float totalPulse = 0;
        int count = 0;

        for (BloodPressureReading reading : last8Readings) {
            if (reading.getSystolic() != 2047 && reading.getDiastolic() != 2047 && reading.getPulse() != 2047) {
                totalSystolic += reading.getSystolic();
                totalDiastolic += reading.getDiastolic();
                totalPulse += reading.getPulse();
                count++;
            }
        }

        final float averageSystolic = count > 0 ? totalSystolic / count : 0;
        final float averageDiastolic = count > 0 ? totalDiastolic / count : 0;
        final float averagePulse = count > 0 ? totalPulse / count : 0;

        TextView systolicTextView = binding.getRoot().findViewById(R.id.text_average_systolic);
        TextView diastolicTextView = binding.getRoot().findViewById(R.id.text_average_diastolic);
        //TextView pulseTextView = binding.getRoot().findViewById(R.id.text_average_pulse);

        systolicTextView.setText(String.format("Average Systolic: %.1f", averageSystolic));
        diastolicTextView.setText(String.format("Average Diastolic: %.1f", averageDiastolic));
        //pulseTextView.setText(String.format("Average Pulse: %.1f", averagePulse));
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void updateChart() {
        List<Entry> systolicEntries = new ArrayList<>();
        List<Entry> diastolicEntries = new ArrayList<>();
        List<Entry> pulseEntries = new ArrayList<>();

        for (int i = 0; i < readings.size(); i++) {
            BloodPressureReading reading = readings.get(i);
            systolicEntries.add(new Entry(i, reading.getSystolic()));
            diastolicEntries.add(new Entry(i, reading.getDiastolic()));
            pulseEntries.add(new Entry(i, reading.getPulse()));
        }

        LineData lineData = getLineData(systolicEntries, diastolicEntries, pulseEntries);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private static @NonNull LineData getLineData(List<Entry> systolicEntries, List<Entry> diastolicEntries, List<Entry> pulseEntries) {
        LineDataSet systolicDataSet = new LineDataSet(systolicEntries, "Systolic");
        systolicDataSet.setColor(Color.RED);
        systolicDataSet.setLineWidth(2f);
        systolicDataSet.setCircleColor(Color.RED);

        LineDataSet diastolicDataSet = new LineDataSet(diastolicEntries, "Diastolic");
        diastolicDataSet.setColor(Color.BLUE);
        diastolicDataSet.setLineWidth(2f);
        diastolicDataSet.setCircleColor(Color.BLUE);

        LineDataSet pulseDataSet = new LineDataSet(pulseEntries, "Pulse");
        pulseDataSet.setColor(Color.GREEN);
        pulseDataSet.setLineWidth(2f);
        pulseDataSet.setCircleColor(Color.GREEN);

        return new LineData(systolicDataSet, diastolicDataSet, pulseDataSet);
    }
}

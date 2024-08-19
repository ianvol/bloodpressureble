package com.example.bloodpressure.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodpressure.db.BloodPressureDao;
import com.example.bloodpressure.gatt.BPMeasurement;
import com.example.bloodpressure.gatt.BloodPressureAdapter;
import com.example.bloodpressure.gatt.BloodPressureReading;
import com.example.bloodpressure.gatt.BluetoothLeService;
import com.example.bloodpressure.R;
import com.example.bloodpressure.databinding.FragmentHomeBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int PERMISSION_REQUEST_BLUETOOTH = 3;
    private static final String TARGET_DEVICE_NAME = "UA-651BLE";
    private static final String TAG = "HomeFragment";
    private static final long SCAN_INTERVAL_MS = 12000;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private FragmentHomeBinding binding;
    private BluetoothLeService bluetoothLeService;
    private boolean isBoundService = false;

    private BloodPressureAdapter adapter;
    private final List<BloodPressureReading> readings = new ArrayList<>();

    private BloodPressureDao bloodPressureDao;
    private LineChart lineChart;

    private final Handler handler = new Handler();

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            bluetoothLeService = binder.getService();
            bluetoothLeService.initialize(bluetoothAdapter);
            isBoundService = true;
            Log.d(TAG, "BluetoothLeService bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
            isBoundService = false;
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        checkPermissions();

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BloodPressureAdapter(readings);
        recyclerView.setAdapter(adapter);

        bloodPressureDao = new BloodPressureDao(getActivity());

        // Load previously saved readings from the database
        new Thread(() -> {
            List<BloodPressureReading> savedReadings = bloodPressureDao.getLast8Readings();
            BloodPressureReading lastReading = bloodPressureDao.getLatestReading();
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
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), BluetoothLeService.class);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        requireActivity().startService(intent);
        startBLEScan();
        handler.post(scanRunnable);

        IntentFilter filter = new IntentFilter("com.example.bloodpressure.BLOOD_PRESSURE_UPDATE");
        requireActivity().registerReceiver(bloodPressureReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBoundService) {
            requireActivity().unbindService(serviceConnection);
            isBoundService = false;
        }
        handler.removeCallbacks(scanRunnable);

        requireActivity().unregisterReceiver(bloodPressureReceiver);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, PERMISSION_REQUEST_BLUETOOTH);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, PERMISSION_REQUEST_BLUETOOTH);
            }
        }
    }

    private void startBLEScan() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Log.d(TAG, "Starting BLE scan");
            bluetoothLeScanner.startScan(scanCallback);
            Toast.makeText(getActivity(), "Scanning for BLE devices...", Toast.LENGTH_SHORT).show();
        } else {
            checkPermissions();
        }
    }
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String deviceName = result.getDevice().getName();
            String deviceAddress = result.getDevice().getAddress();
            if (deviceName != null && deviceName.contains(TARGET_DEVICE_NAME)) {
                Log.d(TAG, "Target device found: " + deviceName);
                bluetoothLeScanner.stopScan(this);
                if (isBoundService) {
                    boolean success = bluetoothLeService.connect(deviceAddress);
                    if (success) {
                        Log.i(TAG, "Connected to device: " + deviceAddress);
                    } else {
                        Log.e(TAG, "Failed to connect to device: " + deviceAddress);
                    }
                } else {
                    Log.e(TAG, "Service not bound");
                }
            }
        }
    };

    private final BroadcastReceiver bloodPressureReceiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                float systolic = bundle.getFloat(BPMeasurement.KEY_SYSTOLIC);
                float diastolic = bundle.getFloat(BPMeasurement.KEY_DIASTOLIC);
                float pulse = bundle.getFloat(BPMeasurement.KEY_PULSE_RATE);
                int year = bundle.getInt(BPMeasurement.KEY_YEAR);
                int month = bundle.getInt(BPMeasurement.KEY_MONTH);
                int day = bundle.getInt(BPMeasurement.KEY_DAY);
                int hours = bundle.getInt(BPMeasurement.KEY_HOURS);
                int minutes = bundle.getInt(BPMeasurement.KEY_MINUTES);
                int seconds = bundle.getInt(BPMeasurement.KEY_SECONDS);

                BloodPressureReading reading = new BloodPressureReading(systolic, diastolic, pulse, year, month, day, hours, minutes, seconds);
                readings.add(reading);
                adapter.notifyDataSetChanged();

                @SuppressLint("DefaultLocale") String dateTime = String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hours, minutes, seconds);

                new Thread(() -> bloodPressureDao.insertReading(systolic, diastolic, pulse, dateTime)).start();

                updateChart();

                Log.d(TAG, "Blood Pressure Readings - Systolic: " + systolic +
                        ", Diastolic: " + diastolic +
                        ", Pulse: " + pulse);
                Log.d(TAG, "Date: " + dateTime);
            }
        }
    };

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

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            startBLEScan();
            handler.postDelayed(this, SCAN_INTERVAL_MS);
        }
    };
}

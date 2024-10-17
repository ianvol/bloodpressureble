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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private ImageView wifiStatus;
    private ImageView wifiStatusGreen;
    private TextView connectionStatus;

    private TextView systolicGoalTextView;
    private TextView diastolicGoalTextView;

    private TextView goalInfo;

    private Handler handler2 = new Handler();
    private Runnable updateGoalsRunnable;

    private BloodPressureAdapter adapter;
    private final List<BloodPressureReading> readings = new ArrayList<>();

    private BloodPressureDao bloodPressureDao;

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

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        adapter = new BloodPressureAdapter(readings);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        wifiStatus = root.findViewById(R.id.wifiStatus);
        wifiStatusGreen = root.findViewById(R.id.wifiStatusGreen);
        connectionStatus = root.findViewById(R.id.connectionStatus);

        wifiStatus.setVisibility(View.VISIBLE);
        wifiStatusGreen.setVisibility(View.GONE);

        // Find the goal TextViews
        systolicGoalTextView = root.findViewById(R.id.systolicGoal);
        diastolicGoalTextView = root.findViewById(R.id.diastolicGoal);

        goalInfo = root.findViewById(R.id.goalInfo);

        // Load goals into TextViews
        //loadGoals();

        updateGoalsRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    // Load and update goals
                    loadGoals();
                    // Schedule the next update after 500 ms
                    handler2.postDelayed(this, 750);
                }
            }
        };

        // Start updating goals
        handler2.post(updateGoalsRunnable);

        TextView systolicText = root.findViewById(R.id.text_systolic);
        TextView diastolicText = root.findViewById(R.id.text_diastolic);
        TextView pulseText = root.findViewById(R.id.text_pulse);

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

        bloodPressureDao = new BloodPressureDao(getActivity());

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
        handler2.post(updateGoalsRunnable);

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

    @SuppressLint("SetTextI18n")
    private void loadGoals() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("BloodPressurePrefs", Context.MODE_PRIVATE);

        int systolicGoal = sharedPreferences.getInt("systolic_goal", 0);
        int diastolicGoal = sharedPreferences.getInt("diastolic_goal", 0);

        Log.d("HomeFragment", "Loaded Systolic: " + systolicGoal + ", Diastolic: " + diastolicGoal);

        systolicGoalTextView.setText("Systolic Goal: " + systolicGoal);
        diastolicGoalTextView.setText("Diastolic Goal: " + diastolicGoal);
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
            wifiStatus.setVisibility(View.VISIBLE);
            wifiStatusGreen.setVisibility(View.GONE);
            connectionStatus.setText("Disconnected");
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
                bluetoothLeScanner.stopScan(scanCallback);
                if (isBoundService) {
                    boolean success = bluetoothLeService.connect(deviceAddress);
                    if (success) {
                        Log.i(TAG, "Connected to device: " + deviceAddress);
                        wifiStatus.setVisibility(View.GONE);
                        wifiStatusGreen.setVisibility(View.VISIBLE);
                        connectionStatus.setText("Connected");
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
        @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
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

                Log.d(TAG, "Blood Pressure Readings - Systolic: " + systolic +
                        ", Diastolic: " + diastolic +
                        ", Pulse: " + pulse);
                Log.d(TAG, "Date: " + dateTime);

                TextView systolicTextView = requireActivity().findViewById(R.id.text_systolic);
                systolicTextView.setText("Systolic: " + systolic);

                TextView diastolicTextView = requireActivity().findViewById(R.id.text_diastolic);
                diastolicTextView.setText("Diastolic: " + diastolic);

                TextView pulseTextView = requireActivity().findViewById(R.id.text_pulse);
                pulseTextView.setText("Pulse: " + pulse);

                try {
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("BloodPressurePrefs", Context.MODE_PRIVATE);

                    int systolicGoal = sharedPreferences.getInt("systolic_goal", 0);  // Default to 0 if not set
                    int diastolicGoal = sharedPreferences.getInt("diastolic_goal", 0);  // Default to 0 if not set

                    if (systolic < systolicGoal || diastolic < diastolicGoal) {
                        goalInfo.setText(R.string.good_goal);
                    } else {
                        goalInfo.setText(R.string.bad_goal);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error retrieving systolic/diastolic goal from SharedPreferences", e);
                    goalInfo.setText("Invalid goal value");
                }
            }
        }
    };

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            startBLEScan();
            handler.postDelayed(this, SCAN_INTERVAL_MS);
        }
    };
}

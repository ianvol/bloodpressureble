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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodpressure.gatt.BPMeasurement;
import com.example.bloodpressure.gatt.BloodPressureAdapter;
import com.example.bloodpressure.gatt.BloodPressureReading;
import com.example.bloodpressure.gatt.BluetoothLeService;
import com.example.bloodpressure.R;
import com.example.bloodpressure.databinding.FragmentHomeBinding;
import com.example.bloodpressure.ui.dashboard.DashboardFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private static final int PERMISSION_REQUEST_BLUETOOTH = 3;
    private static final String TARGET_DEVICE_NAME = "UA-651BLE";
    private static final String TAG = "HomeFragment";
    private static final long SCAN_INTERVAL_MS = 10000; // 10 seconds

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private FragmentHomeBinding binding;
    private BluetoothLeService bluetoothLeService;
    private boolean isBoundService = false;
    private boolean isConnected = false;

    private BloodPressureAdapter adapter;
    private final List<BloodPressureReading> readings = new ArrayList<>();

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


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final Button scanButton = root.findViewById(R.id.btn_ua_651BLE);
        //scanButton.setOnClickListener(v -> startBLEScan());

        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            getActivity().finish();
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Log.d("HomeFragment", "BroadcastReceivers unregistered");
        } catch (IllegalArgumentException e) {
            Log.e("HomeFragment", "BroadcastReceiver not registered", e);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBoundService) {
            requireActivity().unbindService(serviceConnection);
            isBoundService = false;
        }
        handler.removeCallbacks(logRunnable);

        requireActivity().unregisterReceiver(bloodPressureReceiver);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    private void startBLEScan() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
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
                        //navigateToDashboard();
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
                float pulse = bundle.getFloat(BPMeasurement.KEY_PULSE);

                BloodPressureReading reading = new BloodPressureReading(systolic, diastolic, pulse);
                readings.add(reading);
                adapter.notifyDataSetChanged();

                Log.d(TAG, "Blood Pressure Readings - Systolic: " + systolic +
                        ", Diastolic: " + diastolic +
                        ", Pulse: " + pulse);
            }
        }
    };

    private void navigateToDashboard() {
        getParentFragmentManager().beginTransaction().replace(R.id.navigation_dashboard, new DashboardFragment()).commit();
    }


    private void refreshBloodPressureLayout() {
    }

    private final Runnable logRunnable = new Runnable() {
        @Override
        public void run() {
            if (isConnected) {
                Log.i(TAG, "Device is connected");
                if (bluetoothLeService != null) {
                    //bluetoothLeService.readBloodPressureData();
                }
                handler.postDelayed(this, 2000); // Repeat every 2 seconds
            }
        }
    };

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Starting BLE scan");
                bluetoothLeScanner.startScan(scanCallback);
                Toast.makeText(getActivity(), "Scanning for BLE devices...", Toast.LENGTH_SHORT).show();
            } else {
                checkPermissions();
            }
            handler.postDelayed(this, SCAN_INTERVAL_MS);
        }
    };
}

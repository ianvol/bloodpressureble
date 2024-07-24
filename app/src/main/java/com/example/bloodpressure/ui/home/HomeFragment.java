package com.example.bloodpressure.ui.home;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bloodpressure.R;
import com.example.bloodpressure.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 3;
    private static final long SCAN_DURATION = 10000;
    private static final String TARGET_DEVICE_NAME = "UA-651BLE";
    private static final String TAG = "HomeFragment";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private long startTime;
    private boolean isScanning = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Button UA651BLEButton = binding.btnUa651BLE;
        UA651BLEButton.setOnClickListener(v -> {
            Log.d(TAG, "UA-651BLE button clicked");
            Toast.makeText(getActivity(), "UA-651BLE button clicked", Toast.LENGTH_SHORT).show();
            startBLEScan();
        });

        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return root;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (arePermissionsGranted()) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } else {
            requestPermissions();
        }

        return root;
    }

    private boolean arePermissionsGranted() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, PERMISSION_REQUEST_BLUETOOTH);
    }

    private void startBLEScan() {
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (arePermissionsGranted()) {
            startTime = System.currentTimeMillis();
            isScanning = true;
            Log.d(TAG, "Starting BLE scan");
            bluetoothLeScanner.startScan(scanCallback);
            Toast.makeText(getActivity(), "Scanning for BLE devices...", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Permissions not granted, requesting permissions");
            requestPermissions();
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "Device found: " + result.getDevice().getName() + " - " + result.getDevice().getAddress());
            if (result.getDevice().getName() != null && result.getDevice().getName().contains(TARGET_DEVICE_NAME)) {
                stopScanning();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted, reinitializing BluetoothLeScanner");
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                startBLEScan();
            } else {
                Toast.makeText(getActivity(), "Bluetooth permissions required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopScanning() {
        if (isScanning) {
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
            isScanning = false;
            long scanDuration = System.currentTimeMillis() - startTime;
            if (scanDuration < SCAN_DURATION) {
                Log.i(TAG, "Stopped scanning early: " + scanDuration + "ms");
            } else {
                Log.i(TAG, "Stopped scanning after " + SCAN_DURATION + "ms");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isScanning) {
            stopScanning();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
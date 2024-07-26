package com.example.bloodpressure.ui.home;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.bloodpressure.gatt.BluetoothLeService;
import com.example.bloodpressure.R;
import com.example.bloodpressure.databinding.FragmentHomeBinding;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private static final int PERMISSION_REQUEST_BLUETOOTH = 3;
    private static final String TARGET_DEVICE_NAME = "UA-651BLE";
    private static final String TAG = "HomeFragment";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private FragmentHomeBinding binding;
    private BluetoothLeService bluetoothLeService;
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder binder = (BluetoothLeService.LocalBinder) service;
            bluetoothLeService = binder.getService();
            bluetoothLeService.initialize(bluetoothAdapter);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
            isBound = false;
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button scanButton = root.findViewById(R.id.btn_ua_651BLE);
        scanButton.setOnClickListener(v -> startBLEScan());

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

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), BluetoothLeService.class);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        requireActivity().startService(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            requireActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
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
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
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
            Log.d(TAG, "Device found: " + deviceName + " - " + deviceAddress);

            if (deviceName != null && deviceName.contains(TARGET_DEVICE_NAME)) {
                Log.d(TAG, "Target device found: " + deviceName);
                bluetoothLeScanner.stopScan(this);
                if (isBound) {
                    boolean success = bluetoothLeService.connect(deviceAddress);
                    if (success) {
                        Log.i(TAG, "Attempting to connect to device: " + deviceAddress);
                    } else {
                        Log.e(TAG, "Failed to connect to device: " + deviceAddress);
                    }
                } else {
                    Log.e(TAG, "Service not bound");
                }
            }
        }
    };
}

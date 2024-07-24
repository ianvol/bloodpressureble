package com.example.bloodpressure;

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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.bloodpressure.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 3;
    private static final long SCAN_DURATION = 10000;
    private static final String TARGET_DEVICE_NAME = "UA-651BLE";
    private static final String TAG = "MainActivity";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private long startTime;
    private boolean isScanning = false;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Button UA651BLEButton = findViewById(R.id.btn_ua_651BLE); // Use the correct ID
        if (UA651BLEButton != null) {
            UA651BLEButton.setOnClickListener(v -> {
                Log.d(TAG, "UA-651BLE button clicked");
                Toast.makeText(MainActivity.this, "UA-651BLE button clicked", Toast.LENGTH_SHORT).show();
                startBLEScan();
            });
        } else {
            Log.e(TAG, "Button UA651BLE is null");
        }

        // Initialize Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initialize BluetoothLeScanner
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // Check and request necessary permissions
        if (arePermissionsGranted()) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner(); // Ensure initialization
        } else {
            requestPermissions();
        }
    }

    private boolean arePermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, PERMISSION_REQUEST_BLUETOOTH);
    }

    private void startBLEScan() {
        // Ensure bluetoothLeScanner is initialized
        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (arePermissionsGranted()) {
            startTime = System.currentTimeMillis();
            isScanning = true;
            Log.d(TAG, "Starting BLE scan");
            bluetoothLeScanner.startScan(scanCallback);
            Toast.makeText(this, "Scanning for BLE devices...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Bluetooth permissions required.", Toast.LENGTH_SHORT).show();
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
    protected void onPause() {
        super.onPause();
        if (isScanning) {
            stopScanning();
        }
    }
}

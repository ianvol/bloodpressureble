package com.example.bloodpressure.gatt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BluetoothLeService extends Service {
    private static final String TAG = "BluetoothLeService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public void initialize(BluetoothAdapter adapter) {
        bluetoothAdapter = adapter;
    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address. Unable to connect.");
            return false;
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Successfully connected to the device.");
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.i(TAG, "Service discovered: " + service.getUuid());
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };
}

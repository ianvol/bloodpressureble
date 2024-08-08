package com.example.bloodpressure.gatt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class BluetoothLeService extends Service {
    private static final String TAG = "BluetoothLeService";

    public static final String ACTION_BLE_SERVICE = "com.example.bloodpressure.ACTION_BLE_SERVICE";
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String EXTRA_VALUE = "EXTRA_VALUE";
    public static final String EXTRA_SERVICE_UUID = "EXTRA_SERVICE_UUID";
    public static final String EXTRA_CHARACTERISTIC_UUID = "EXTRA_CHARACTERISTIC_UUID";
    public static final String EXTRA_STATUS = "EXTRA_STATUS";
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";

    public static final String TYPE_GATT_CONNECTED = "Connected device";
    public static final String TYPE_GATT_DISCONNECTED = "Disconnected device";
    public static final String TYPE_GATT_ERROR = "Gatt Error";
    public static final String TYPE_GATT_SERVICES_DISCOVERED = "Discovered services";
    public static final String TYPE_CHARACTERISTIC_READ = "Read characteristic";
    public static final String TYPE_CHARACTERISTIC_WRITE = "Write characteristic";
    public static final String TYPE_CHARACTERISTIC_CHANGED = "Characteristic changed";
    public static final String TYPE_DESCRIPTOR_READ = "Read descriptor";
    public static final String TYPE_DESCRIPTOR_WRITE = "Write descriptor";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initialize(BluetoothAdapter adapter) {
        bluetoothAdapter = adapter;
    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (bluetoothGatt != null) {
            Log.d(TAG, "Closing existing BluetoothGatt connection.");
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
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


    public void disconnect() {
        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized.");
            return;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice device = gatt.getDevice();
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT error. Status: " + status);
                sendBroadcast(TYPE_GATT_ERROR, device, status);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                sendBroadcast(TYPE_GATT_CONNECTED, device, status);
                Log.i(TAG, "Connected to GATT server.");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                sendBroadcast(TYPE_GATT_DISCONNECTED, device, status);
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothDevice device = gatt.getDevice();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(TYPE_GATT_SERVICES_DISCOVERED, device, status);
                Log.i(TAG, "Services discovered.");
                enableNotifications();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            sendBroadcast(TYPE_CHARACTERISTIC_READ, characteristic, status);
            Log.d(TAG, "Characteristic read. Status: " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            sendBroadcast(TYPE_CHARACTERISTIC_WRITE, characteristic, status);
            Log.d(TAG, "Characteristic write. Status: " + status);
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            sendBroadcast(TYPE_CHARACTERISTIC_CHANGED, characteristic, BluetoothGatt.GATT_SUCCESS);
            Bundle bundle = BPMeasurement.readCharacteristic(characteristic);
            Intent intent = new Intent("com.example.bloodpressure.BLOOD_PRESSURE_UPDATE");
            intent.putExtras(bundle);
            sendBroadcast(intent);

            Log.d(TAG, "Characteristic changed: " + characteristic.getUuid().toString());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothDevice device = gatt.getDevice();
            sendBroadcast(TYPE_DESCRIPTOR_READ, descriptor, status);
            Log.d(TAG, "Descriptor read. Status: " + status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothDevice device = gatt.getDevice();
            sendBroadcast(TYPE_DESCRIPTOR_WRITE, descriptor, status);
            Log.d(TAG, "Descriptor write. Status: " + status);
        }
    };

    private void sendBroadcast(String type, BluetoothDevice device, int status) {
        Intent intent = new Intent(ACTION_BLE_SERVICE);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_ADDRESS, device.getAddress());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: " + type);
    }

    private void sendBroadcast(String type, BluetoothGattCharacteristic characteristic, int status) {
        Intent intent = new Intent(ACTION_BLE_SERVICE);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_SERVICE_UUID, characteristic.getService().getUuid().toString());
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_VALUE, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: " + type);
    }

    private void sendBroadcast(String type, BluetoothGattDescriptor descriptor, int status) {
        String serviceUuidString = descriptor.getCharacteristic().getService().getUuid().toString();
        String characteristicUuidString = descriptor.getCharacteristic().getUuid().toString();

        Intent intent = new Intent(ACTION_BLE_SERVICE);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_SERVICE_UUID, serviceUuidString);
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristicUuidString);
        intent.putExtra(EXTRA_VALUE, descriptor.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcast sent: " + type);
    }

    public static UUID uuidFromShortString(String uuid) {
        return UUID.fromString(String.format("0000%s-0000-1000-8000-00805f9b34fb", uuid));
    }

    public void enableNotifications() {
        UUID serviceUuid = uuidFromShortString("1810");
        UUID characteristicUuid = uuidFromShortString("2A35");

        if (bluetoothGatt == null) {
            Log.w(TAG, "BluetoothGatt not initialized.");
            return;
        }

        BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
        if (service == null) {
            Log.w(TAG, "Service not found: " + serviceUuid);
            return;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        if (characteristic == null) {
            Log.w(TAG, "Characteristic not found: " + characteristicUuid);
            return;
        }

        bluetoothGatt.setCharacteristicNotification(characteristic, true);

        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }
}

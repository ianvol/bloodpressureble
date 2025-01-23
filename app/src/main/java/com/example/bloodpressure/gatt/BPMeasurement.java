package com.example.bloodpressure.gatt;

import static com.example.bloodpressure.base.GattService.KEY_DAY;
import static com.example.bloodpressure.base.GattService.KEY_HOURS;
import static com.example.bloodpressure.base.GattService.KEY_IRREGULAR_PULSE_DETECTION;
import static com.example.bloodpressure.base.GattService.KEY_MEAS_ARTERIAL_PRESSURE;
import static com.example.bloodpressure.base.GattService.KEY_MINUTES;
import static com.example.bloodpressure.base.GattService.KEY_MONTH;
import static com.example.bloodpressure.base.GattService.KEY_SECONDS;
import static com.example.bloodpressure.base.GattService.KEY_YEAR;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;

import com.example.bloodpressure.base.GattService;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Locale;

public class BPMeasurement {

    public static final String KEY_UNIT = "unit";
    public static final String KEY_SYSTOLIC = "systolic";
    public static final String KEY_DIASTOLIC = "diastolic";
    public static final String KEY_PULSE_RATE = "pulse_rate";
    public static final String KEY_YEAR = "year";
    public static final String KEY_MONTH = "month";
    public static final String KEY_DAY = "day";
    public static final String KEY_HOURS = "hours";
    public static final String KEY_MINUTES = "minutes";
    public static final String KEY_SECONDS = "seconds";

    public static Bundle readCharacteristic(BluetoothGattCharacteristic characteristic) {

        Bundle bundle = new Bundle();
        int flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        String flagString = Integer.toBinaryString(flag);
        int offset=0;
        for(int index = flagString.length(); 0 < index ; index--) {
            String key = flagString.substring(index-1 , index);

            if(index == flagString.length()) {
                if(key.equals("0")) {
                    bundle.putString(KEY_UNIT, "mmHg");
                }
                else {
                    bundle.putString(KEY_UNIT, "kPa");
                }
                offset+=1;
                bundle.putFloat(KEY_SYSTOLIC, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                offset+=2;
                bundle.putFloat(KEY_DIASTOLIC, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                offset+=2;
                bundle.putFloat(KEY_MEAS_ARTERIAL_PRESSURE, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                offset+=2;
            }
            else if(index == flagString.length()-1) {
                if(key.equals("1")) {
                    bundle.putInt(KEY_YEAR, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset));
                    offset+=2;
                    bundle.putInt(KEY_MONTH, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                    bundle.putInt(KEY_DAY, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;

                    bundle.putInt(KEY_HOURS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                    bundle.putInt(KEY_MINUTES, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                    bundle.putInt(KEY_SECONDS, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset));
                    offset+=1;
                }
                else {
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    bundle.putInt(KEY_YEAR, calendar.get(Calendar.YEAR));
                    bundle.putInt(KEY_MONTH, calendar.get(Calendar.MONTH)+1);
                    bundle.putInt(KEY_DAY, calendar.get(Calendar.DAY_OF_MONTH));
                    bundle.putInt(KEY_HOURS, calendar.get(Calendar.HOUR));
                    bundle.putInt(KEY_MINUTES, calendar.get(Calendar.MINUTE));
                    bundle.putInt(KEY_SECONDS, calendar.get(Calendar.SECOND));
                }
            }
            else if(index == flagString.length()-2) {
                if(key.equals("1")) {
                    bundle.putFloat(GattService.KEY_PULSE_RATE, characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, offset));
                    offset+=2;
                }
            }
            else if(index == flagString.length()-4) {

                ByteBuffer statusFlagBuffer = ByteBuffer.allocate(2);
                statusFlagBuffer.put(characteristic.getValue()[offset]);
                statusFlagBuffer.put(characteristic.getValue()[offset+1]);

                byte[] statusFlag = statusFlagBuffer.array();

                if ((byte)(statusFlag[0] & (byte)0x04) == (byte)0x04) {
                    bundle.putInt(KEY_IRREGULAR_PULSE_DETECTION, 1); // To be implemented
                } else {
                    bundle.putInt(KEY_IRREGULAR_PULSE_DETECTION, 0);
                }
            }
        }
        return bundle;
    }
}

package com.example.bloodpressure.db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bloodpressure.gatt.BloodPressureReading;

import java.util.ArrayList;
import java.util.List;

public class BloodPressureDao {

    private SQLiteDatabase database;
    private BloodPressureDbHelper dbHelper;

    public BloodPressureDao(Context context) {
        dbHelper = new BloodPressureDbHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void insertReading(float systolic, float diastolic, float pulse) {
        ContentValues values = new ContentValues();
        values.put(BloodPressureDbHelper.COLUMN_SYSTOLIC, systolic);
        values.put(BloodPressureDbHelper.COLUMN_DIASTOLIC, diastolic);
        values.put(BloodPressureDbHelper.COLUMN_PULSE, pulse);

        database.insert(BloodPressureDbHelper.TABLE_NAME, null, values);
    }

    public List<BloodPressureReading> getAllReadings() {
        List<BloodPressureReading> readings = new ArrayList<>();
        Cursor cursor = database.query(BloodPressureDbHelper.TABLE_NAME, null, null, null, null, null, BloodPressureDbHelper.COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                float systolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_SYSTOLIC));
                float diastolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DIASTOLIC));
                float pulse = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_PULSE));

                BloodPressureReading reading = new BloodPressureReading(systolic, diastolic, pulse);
                readings.add(reading);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return readings;
    }
    public void close() {dbHelper.close();}
}

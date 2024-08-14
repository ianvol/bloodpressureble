package com.example.bloodpressure.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bloodpressure.gatt.BloodPressureReading;

import java.util.ArrayList;
import java.util.List;

public class BloodPressureDao {

    private final SQLiteDatabase database;
    private final BloodPressureDbHelper dbHelper;

    public BloodPressureDao(Context context) {
        dbHelper = new BloodPressureDbHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void insertReading(float systolic, float diastolic, float pulse_rate, String dateTime) {
        ContentValues values = new ContentValues();
        values.put(BloodPressureDbHelper.COLUMN_SYSTOLIC, systolic);
        values.put(BloodPressureDbHelper.COLUMN_DIASTOLIC, diastolic);
        values.put(BloodPressureDbHelper.COLUMN_PULSE_RATE, pulse_rate);
        values.put(BloodPressureDbHelper.COLUMN_DATE_TIME, dateTime);

        database.insert(BloodPressureDbHelper.TABLE_NAME, null, values);
    }

    public List<BloodPressureReading> getLast8Readings() {
        List<BloodPressureReading> readings = new ArrayList<>();

        String query = "SELECT * FROM " + BloodPressureDbHelper.TABLE_NAME +
                " ORDER BY " + BloodPressureDbHelper.COLUMN_DATE_TIME + " DESC LIMIT 8";

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                float systolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_SYSTOLIC));
                float diastolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DIASTOLIC));
                float pulse = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_PULSE_RATE));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DATE_TIME));

                String[] dateTimeParts = dateTime.split("[- :]");
                int year = Integer.parseInt(dateTimeParts[0]);
                int month = Integer.parseInt(dateTimeParts[1]);
                int day = Integer.parseInt(dateTimeParts[2]);
                int hours = Integer.parseInt(dateTimeParts[3]);
                int minutes = Integer.parseInt(dateTimeParts[4]);
                int seconds = Integer.parseInt(dateTimeParts[5]);

                BloodPressureReading reading = new BloodPressureReading(systolic, diastolic, pulse, year, month, day, hours, minutes, seconds);
                readings.add(reading);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return readings;
    }

    public List<BloodPressureReading> getAllReadings() {
        List<BloodPressureReading> readings = new ArrayList<>();
        Cursor cursor = database.query(BloodPressureDbHelper.TABLE_NAME, null, null, null, null, null, BloodPressureDbHelper.COLUMN_DATE_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                float systolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_SYSTOLIC));
                float diastolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DIASTOLIC));
                float pulse = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_PULSE_RATE));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DATE_TIME));

                String[] dateTimeParts = dateTime.split("[- :]");
                int year = Integer.parseInt(dateTimeParts[0]);
                int month = Integer.parseInt(dateTimeParts[1]);
                int day = Integer.parseInt(dateTimeParts[2]);
                int hours = Integer.parseInt(dateTimeParts[3]);
                int minutes = Integer.parseInt(dateTimeParts[4]);
                int seconds = Integer.parseInt(dateTimeParts[5]);

                BloodPressureReading reading = new BloodPressureReading(systolic, diastolic, pulse, year, month, day, hours, minutes, seconds);
                readings.add(reading);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return readings;
    }


    public List<BloodPressureReading> getReadingsByDate(String date) {
        List<BloodPressureReading> readings = new ArrayList<>();

        String query = "SELECT * FROM " + BloodPressureDbHelper.TABLE_NAME +
                " WHERE DATE(" + BloodPressureDbHelper.COLUMN_DATE_TIME + ") = ? ORDER BY " + BloodPressureDbHelper.COLUMN_DATE_TIME + " DESC"; // Choose by date - to add

        Cursor cursor = database.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                float systolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_SYSTOLIC));
                float diastolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DIASTOLIC));
                float pulse = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_PULSE_RATE));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DATE_TIME));

                String[] dateTimeParts = dateTime.split("[- :]");
                int year = Integer.parseInt(dateTimeParts[0]);
                int month = Integer.parseInt(dateTimeParts[1]);
                int day = Integer.parseInt(dateTimeParts[2]);
                int hours = Integer.parseInt(dateTimeParts[3]);
                int minutes = Integer.parseInt(dateTimeParts[4]);
                int seconds = Integer.parseInt(dateTimeParts[5]);

                BloodPressureReading reading = new BloodPressureReading(systolic, diastolic, pulse, year, month, day, hours, minutes, seconds);
                readings.add(reading);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return readings;
    }

    public void close() {
        dbHelper.close();
    }
}

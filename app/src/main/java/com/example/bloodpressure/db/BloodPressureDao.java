package com.example.bloodpressure.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.bloodpressure.gatt.BloodPressureReading;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public BloodPressureReading getLatestReading() {
        BloodPressureReading reading = null;

        String query = "SELECT * FROM " + BloodPressureDbHelper.TABLE_NAME +
                " ORDER BY " + BloodPressureDbHelper.COLUMN_DATE_TIME + " DESC LIMIT 1";

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
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

            reading = new BloodPressureReading(systolic, diastolic, pulse, year, month, day, hours, minutes, seconds);
        }

        cursor.close();
        return reading;
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

                if (systolic != 2047 || diastolic != 2047) {
                    readings.add(reading);
                }

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

    public void logAllTimestamps() {
        String query = "SELECT " + BloodPressureDbHelper.COLUMN_DATE_TIME + ", "
                + BloodPressureDbHelper.COLUMN_SYSTOLIC + ", "
                + BloodPressureDbHelper.COLUMN_DIASTOLIC +
                " FROM " + BloodPressureDbHelper.TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DATE_TIME));
                float systolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_SYSTOLIC));
                float diastolic = cursor.getFloat(cursor.getColumnIndexOrThrow(BloodPressureDbHelper.COLUMN_DIASTOLIC));

                Log.d("BloodPressureDao", "Timestamp: " + dateTime + ", Systolic: " + systolic + ", Diastolic: " + diastolic);
            } while (cursor.moveToNext());
        } else {
            Log.d("BloodPressureDao", "No records found in the database.");
        }

        cursor.close();
    }


    public List<BloodPressureReading> getReadingsLast24Hours() {
        List<BloodPressureReading> readings = new ArrayList<>();

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Log.d("BloodPressureDao", "Current Time: " + now);

        String query = "SELECT * FROM " + BloodPressureDbHelper.TABLE_NAME +
                " WHERE " + BloodPressureDbHelper.COLUMN_DATE_TIME + " >= datetime('now', '-30 minutes')" +
                " ORDER BY " + BloodPressureDbHelper.COLUMN_DATE_TIME + " DESC";

        Log.d("BloodPressureDao", "Query: " + query);

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

                if (systolic != 2047 || diastolic != 2047) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long readingTimeMillis = dateTimeToMillis(dateTime);
                    long timeDiff = currentTimeMillis - readingTimeMillis;
                    if (timeDiff <= 8.64e+7) { // 24 hours in milliseconds
                        readings.add(reading);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return readings;
    }

    private long dateTimeToMillis(String dateTime) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(dateTime);
            assert date != null;
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<BloodPressureReading> getReadingsLast72Hours() {
        List<BloodPressureReading> readings = new ArrayList<>();

        String query = "SELECT * FROM " + BloodPressureDbHelper.TABLE_NAME +
                " WHERE " + BloodPressureDbHelper.COLUMN_DATE_TIME + " >= datetime('now', '-3 days')" +
                " ORDER BY " + BloodPressureDbHelper.COLUMN_DATE_TIME + " DESC";

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

                if (systolic != 2047 || diastolic != 2047) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long readingTimeMillis = dateTimeToMillis(dateTime);
                    long timeDiff = currentTimeMillis - readingTimeMillis;
                    if (timeDiff <= 2.592e+8) { // 3 days in milliseconds
                        readings.add(reading);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return readings;
    }

    public List<BloodPressureReading> getReadingsLast7Days() {
        List<BloodPressureReading> readings = new ArrayList<>();

        String query = "SELECT * FROM " + BloodPressureDbHelper.TABLE_NAME +
                " WHERE " + BloodPressureDbHelper.COLUMN_DATE_TIME + " >= datetime('now', '-7 days')" +
                " ORDER BY " + BloodPressureDbHelper.COLUMN_DATE_TIME + " DESC";

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

                if (systolic != 2047 || diastolic != 2047) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long readingTimeMillis = dateTimeToMillis(dateTime);
                    long timeDiff = currentTimeMillis - readingTimeMillis;
                    if (timeDiff <= 6.408e+8) { // 1 week in milliseconds
                        readings.add(reading);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return readings;
    }


    public void close() {
        dbHelper.close();
    }
}

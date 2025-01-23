package com.example.bloodpressure.gatt;

public class BloodPressureReading {
    private final float systolic;
    private final float diastolic;
    private final float pulse;
    private final int year;
    private final int month;
    private final int day;
    private final int hours;
    private final int minutes;
    private final int seconds;

    public BloodPressureReading(float systolic, float diastolic, float pulse, int year, int month, int day, int hours, int minutes, int seconds) {
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public float getSystolic() { return systolic; }

    public float getDiastolic() { return diastolic; }

    public float getPulse() { return pulse; }

    public int getYear() { return year; }

    public int getMonth() { return month; }

    public int getDay() { return day; }

    public int getHours() { return hours; }

    public int getMinutes() { return minutes; }

    public int getSeconds() { return seconds; }
}

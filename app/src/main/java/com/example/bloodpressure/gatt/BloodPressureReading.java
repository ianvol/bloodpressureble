package com.example.bloodpressure.gatt;

public class BloodPressureReading {
    private final float systolic;
    private final float diastolic;
    private final float pulse;

    public BloodPressureReading(float systolic, float diastolic, float pulse) {
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
    }

    public float getSystolic() {
        return systolic;
    }

    public float getDiastolic() {
        return diastolic;
    }

    public float getPulse() {
        return pulse;
    }
}

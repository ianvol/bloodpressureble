package com.example.bloodpressure.ui.dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bloodpressure.db.BloodPressureDao;
import com.example.bloodpressure.gatt.BloodPressureReading;

import java.util.List;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final BloodPressureDao bloodPressureDao;

    private static final String TAG = "DashboardViewModel";

    public DashboardViewModel(BloodPressureDao bloodPressureDao) {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
        this.bloodPressureDao = bloodPressureDao;
    }

    public LiveData<String> getText() {
        return mText;
    }

    private void loadAverageReading(){ // Old code - usage TBD
        new Thread(() -> {
            List<BloodPressureReading> last8Readings = bloodPressureDao.getLast8Readings();

            float totalSystolic = 0;
            float totalDiastolic = 0;
            float totalPulse = 0;
            int count = 0;

            for (BloodPressureReading reading : last8Readings){
                if (reading.getSystolic() != 2047 && reading.getDiastolic() != 2047){
                    totalSystolic += reading.getSystolic();
                    totalDiastolic += reading.getDiastolic();
                    totalPulse += reading.getPulse();
                    count++;
                }
            }

            if (count > 0) {
                float avgSystolic = totalSystolic / count;
                float avgDiastolic = totalDiastolic / count;
                float avgPulse = totalPulse / count;
                Log.i(TAG, "Systolic: " + avgSystolic);
                Log.i(TAG, "Diastolic: " + avgDiastolic);
                Log.i(TAG, "Pulse: " + avgPulse);
            } else {
                Log.e(TAG, "Error in calculating average reading!");
            }


        }).start();
    }
}
package com.example.bloodpressure.ui.dashboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.bloodpressure.db.BloodPressureDao;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {

    private final BloodPressureDao bloodPressureDao;

    public DashboardViewModelFactory(BloodPressureDao bloodPressureDao) {
        this.bloodPressureDao = bloodPressureDao;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(bloodPressureDao);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

package com.example.bloodpressure.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.bloodpressure.databinding.FragmentDashboardBinding;
import com.example.bloodpressure.db.BloodPressureDao;
import com.example.bloodpressure.gatt.BloodPressureReading;

import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;

    private static final String TAG = "DashboardFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BloodPressureDao dao = new BloodPressureDao(getActivity());
        DashboardViewModelFactory factory = new DashboardViewModelFactory(dao);
        dashboardViewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        loadAverageReading();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadAverageReading() {
        new Thread(() -> {
            // Access database and get last 8 readings
            BloodPressureDao dao = new BloodPressureDao(getActivity());
            List<BloodPressureReading> last8Readings = dao.getLast8Readings();
            dao.close();

            // Calculate average, disregarding any readings with a value of 2047
            float totalSystolic = 0;
            float totalDiastolic = 0;
            float totalPulse = 0;
            int count = 0;

            for (BloodPressureReading reading : last8Readings) {
                if (reading.getSystolic() != 2047 && reading.getDiastolic() != 2047 && reading.getPulse() != 2047) {
                    totalSystolic += reading.getSystolic();
                    totalDiastolic += reading.getDiastolic();
                    totalPulse += reading.getPulse();
                    count++;
                }
            }

            final float averageSystolic = count > 0 ? totalSystolic / count : 0;
            final float averageDiastolic = count > 0 ? totalDiastolic / count : 0;
            final float averagePulse = count > 0 ? totalPulse / count : 0;

            // Update the UI on the main thread
            requireActivity().runOnUiThread(() -> {
                Log.e(TAG, "Systolic: " + averageSystolic);
                Log.e(TAG, "Diastolic: " + averageDiastolic);
            });
        }).start();
    }
}

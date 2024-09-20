package com.example.bloodpressure.ui.goal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bloodpressure.R;

public class GoalFragment extends Fragment {

    private EditText systolicGoalInput;
    private EditText diastolicGoalInput;

    private static final String goalsFile = "BloodPressurePrefs";
    private static final String SYSTOLIC_GOAL = "systolic_goal";
    private static final String DIASTOLIC_GOAL = "diastolic_goal";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_goal, container, false);

        systolicGoalInput = root.findViewById(R.id.systolic_goal_input);
        diastolicGoalInput = root.findViewById(R.id.diastolic_goal_input);
        Button saveButton = root.findViewById(R.id.button_save_goal);

        loadGoals();

        saveButton.setOnClickListener(v -> {
            String systolicGoal = systolicGoalInput.getText().toString();
            String diastolicGoal = diastolicGoalInput.getText().toString();

            if (systolicGoal.isEmpty() || diastolicGoal.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter both goals", Toast.LENGTH_SHORT).show();
                return;
            }

            saveGoals(systolicGoal, diastolicGoal);
            Toast.makeText(getActivity(), "Goals saved", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    private void saveGoals(String systolicGoal, String diastolicGoal) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(goalsFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SYSTOLIC_GOAL, systolicGoal);
        editor.putString(DIASTOLIC_GOAL, diastolicGoal);
        editor.apply();
    }

    private void loadGoals() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(goalsFile, Context.MODE_PRIVATE);
        String savedSystolicGoal = sharedPreferences.getString(SYSTOLIC_GOAL, "");
        String savedDiastolicGoal = sharedPreferences.getString(DIASTOLIC_GOAL, "");

        if (!savedSystolicGoal.isEmpty()) {
            systolicGoalInput.setText(savedSystolicGoal);
        }
        if (!savedDiastolicGoal.isEmpty()) {
            diastolicGoalInput.setText(savedDiastolicGoal);
        }
    }
}

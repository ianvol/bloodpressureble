package com.example.bloodpressure.ui.goal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_goal, container, false);

        systolicGoalInput = root.findViewById(R.id.systolic_goal_input);
        diastolicGoalInput = root.findViewById(R.id.diastolic_goal_input);
        Button saveButton = root.findViewById(R.id.button_save_goal);

        //loadGoals();

        saveButton.setOnClickListener(v -> {
            int systolicGoal = Integer.parseInt(systolicGoalInput.getText().toString());
            int diastolicGoal = Integer.parseInt(diastolicGoalInput.getText().toString());

/*            if (systolicGoal.isEmpty() || diastolicGoal.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter both goals", Toast.LENGTH_SHORT).show();
                return;
            }*/

            saveGoals(systolicGoal, Integer.parseInt(String.valueOf(diastolicGoal)));
            Toast.makeText(getActivity(), "Goals saved", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    private void saveGoals(int systolicGoal, int diastolicGoal) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("BloodPressurePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("systolic_goal", systolicGoal);
        editor.putInt("diastolic_goal", diastolicGoal);

        editor.apply();
    }


/*    private void loadGoals() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("BloodPressurePrefs", Context.MODE_PRIVATE);
        String systolicGoal = sharedPreferences.getString("systolic_goal", "");
        systolicGoalInput.setText(systolicGoal);
        String diastolicGoal = sharedPreferences.getString("diastolic_goal", "");
        diastolicGoalInput.setText(diastolicGoal);
    }*/
}

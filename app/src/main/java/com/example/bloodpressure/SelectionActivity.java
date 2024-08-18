package com.example.bloodpressure;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.bloodpressure.databinding.ActivityMainBinding;
import com.example.bloodpressure.databinding.SelectionActivityBinding;
import com.example.bloodpressure.ui.dashboard.DashboardFragment;
import com.example.bloodpressure.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SelectionActivity extends AppCompatActivity {

    private SelectionActivityBinding binding;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = SelectionActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.topLeftSquare.setOnClickListener(v -> {
            new Handler(Looper.getMainLooper()).postDelayed(this::moveActivity, 50);
        });

        binding.topRightSquare.setOnClickListener(v -> {
            new Handler(Looper.getMainLooper()).postDelayed(this::moveActivityDashboard, 50);
            navView = findViewById(R.id.nav_view);
            selectDashboardMenuItem(); // Simulate selection of dashboard menu item
        });

        binding.bottomLeftSquare.setOnClickListener(v -> {
            new Handler(Looper.getMainLooper()).postDelayed(this::moveActivityIntroduction, 50);
        });

    }

    private void moveActivity() {
        Intent intent = new Intent(SelectionActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // End previous cleanly
    }

    private void moveActivityDashboard() {
        Intent intent = new Intent(SelectionActivity.this, MainActivity.class);
        intent.putExtra("select_dashboard", true);
        startActivity(intent);
        finish();
    }

    private void moveActivityIntroduction() {
        try {
            Class<?> WelcomeActivity = Class.forName("com.example.bloodpressure.WelcomeActivity");
            Intent intent = new Intent(SelectionActivity.this, WelcomeActivity);
            startActivity(intent);
            finish(); // End previous cleanly
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void selectDashboardMenuItem() {
        navView.setSelectedItemId(R.id.navigation_dashboard); // Select dashboard item
    }
}

package com.example.bloodpressure;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class WelcomeActivity extends AppIntro { // Can be altered as desired

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(
                "Welcome!",
                "This is your blood pressure measuring app!",
                R.drawable.measure,
                getColor(R.color.slide1_background)
        ));

        addSlide(AppIntroFragment.newInstance(
                "Features",
                "See your average results",
                R.drawable.results,
                getColor(R.color.slide2_background)
        ));

        addSlide(AppIntroFragment.newInstance(
                "Get Started",
                "Let's get started!",
                R.drawable.green_tick,
                getColor(R.color.slide3_background)
        ));

        setSkipButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        markWelcomeScreenShown();
        navigateToMain();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        markWelcomeScreenShown();
        navigateToMain();
    }

    private void markWelcomeScreenShown() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("welcome_screen_shown", true)
                .apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigateTo", "dashboard");
        startActivity(intent);
        finish();
    }
}

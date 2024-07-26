package com.example.bloodpressure;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class WelcomeActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(
                "Welcome!",
                "This is the first slide of the introduction",
                R.drawable.bananas,
                getColor(R.color.slide1_background)
        ));

        addSlide(AppIntroFragment.newInstance(
                "Features",
                "Here are some features of our app",
                R.drawable.blueberries,
                getColor(R.color.slide2_background)
        ));

        addSlide(AppIntroFragment.newInstance(
                "Get Started",
                "Let's get started!",
                R.drawable.peaches,
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

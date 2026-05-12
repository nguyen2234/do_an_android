package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import androidx.appcompat.app.AppCompatActivity;

public class ChaoMungActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chao_mung);

        // Load Theme preference
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int currentTheme = prefs.getInt("theme_mode", 0);
        if (currentTheme == 2) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Fade in animation on root view
        View root = getWindow().getDecorView();
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        root.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            long userId = prefs.getLong("user_id", -1);
            if (userId != -1) {
                startActivity(new Intent(ChaoMungActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(ChaoMungActivity.this, DangNhapActivity.class));
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }
}

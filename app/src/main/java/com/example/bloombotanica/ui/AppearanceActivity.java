package com.example.bloombotanica.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.bloombotanica.R;
import androidx.preference.PreferenceManager;

public class AppearanceActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the RadioGroup view
        RadioGroup themeRadioGroup = findViewById(R.id.theme_radio_group);

        // Set the current theme based on the current mode (light or dark)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            themeRadioGroup.check(R.id.light_mode_button);  // Light mode is enabled
        } else {
            themeRadioGroup.check(R.id.dark_mode_button);  // Dark mode is enabled
        }

        // Set an OnCheckedChangeListener for the RadioGroup
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.light_mode_button) {
                // Set light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                saveThemeMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(AppearanceActivity.this, "Light Mode", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.dark_mode_button) {
                // Set dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                saveThemeMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(AppearanceActivity.this, "Dark Mode", Toast.LENGTH_SHORT).show();
            }
        });



        // Handle Cancel Button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());  // Close the activity without saving
    }

    private void saveThemeMode(int mode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("theme_mode", mode);
        editor.apply();
    }
}

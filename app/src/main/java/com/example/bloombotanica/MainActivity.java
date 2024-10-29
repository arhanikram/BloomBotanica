package com.example.bloombotanica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.bloombotanica.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(Color.TRANSPARENT); // Use Color.TRANSPARENT for a transparent status bar

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        }

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, new DashboardFragment())
                    .commit();
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (item.getItemId() == R.id.calendar) {
                selectedFragment = new CalendarFragment();
            } else if(item.getItemId() == R.id.plants) {
                selectedFragment = new PlantsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, selectedFragment)
                        .commit();
            }

            return true;
        });

        //set dashboard as default so it auto opens on start
        binding.bottomNavigationView.setSelectedItemId(R.id.dashboard);

    }
}

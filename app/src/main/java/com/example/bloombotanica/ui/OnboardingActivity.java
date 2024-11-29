package com.example.bloombotanica.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;
import java.util.Locale;

import com.example.bloombotanica.R;

public class OnboardingActivity extends AppCompatActivity {

    private String selectedLanguage = "en";  // Default language is English

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Setup language spinner
        Spinner languageSpinner = findViewById(R.id.languageSpinner);
        String[] languages = {"English", "Spanish", "French", "German"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Get saved language preference
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        selectedLanguage = sharedPreferences.getString("language", "en");  // Default to English

        // Set the spinner to the previously selected language
        int position = getLanguagePosition(selectedLanguage);
        languageSpinner.setSelection(position);

        // Debug log to check interaction
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                selectedLanguage = parentView.getItemAtPosition(position).toString().toLowerCase();
                Toast.makeText(OnboardingActivity.this, "Selected: " + selectedLanguage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Setup name input and continue button
        EditText nameInput = findViewById(R.id.nameInput);
        Button continueButton = findViewById(R.id.continueButton);

        continueButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            if (!name.isEmpty()) {
                // Save language preference
                saveLanguagePreference(selectedLanguage);

                // Change the locale based on selected language
                setLocale(selectedLanguage);

                // Proceed to next activity
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", name);
                editor.putBoolean("isFirstLaunch", false);
                editor.apply();

                // Start MainActivity and finish OnboardingActivity
                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                finish();
            } else {
                nameInput.setError("Please enter your name");
            }
        });
    }

    // Method to set the locale (language)
    private void setLocale(String language) {
        Locale locale;
        switch (language) {
            case "spanish":
                locale = new Locale("es", "CO");  // Colombian Spanish
                break;
            case "french":
                locale = new Locale("fr", "CA");  // Canadian French
                break;
            case "german":
                locale = new Locale("de");  // German
                break;
            default:
                locale = new Locale("en");  // English
                break;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    // Method to save the selected language in SharedPreferences
    private void saveLanguagePreference(String language) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", language);
        editor.apply();
    }

    // Method to map language code to spinner position
    private int getLanguagePosition(String language) {
        switch (language) {
            case "spanish":
                return 1; // Spanish
            case "french":
                return 2; // French
            case "german":
                return 3; // German
            default:
                return 0; // English
        }
    }
}

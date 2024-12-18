package com.example.bloombotanica.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.example.bloombotanica.R;
import com.github.chrisbanes.photoview.BuildConfig;

public class SettingsFragment extends Fragment {

    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        applySavedThemeMode();

        // Account Information Section Click Listener (Popup)
        rootView.findViewById(R.id.account_info_layout).setOnClickListener(v -> {
            showAccountInfoDialog();
        });

        // Appearance Section Click Listener (Navigate to AppearanceActivity)
        rootView.findViewById(R.id.appearance_layout).setOnClickListener(v -> {

            //APPEARANCE ACTIVITY CURRENTLY NOT WORKING - Going to use popup for now.

            // Start AppearanceActivity to change the theme
            //Intent intent = new Intent(getActivity(), AppearanceActivity.class);
            //startActivity(intent);

            // Create a PopupMenu for the theme options
            PopupMenu popup = new PopupMenu(getContext(), v);  // Use the `v` (the clicked view)
            popup.getMenuInflater().inflate(R.menu.theme_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.light_mode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }

                // Save the selected theme mode to SharedPreferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("theme_mode", menuItem.getItemId() == R.id.light_mode ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
                editor.apply();  // Apply changes asynchronously

                return true;
            });

            popup.show();

        });

//        // Notifications Section Click Listener (Popup for Push Notifications)
//        rootView.findViewById(R.id.notifications_layout).setOnClickListener(v -> {
//            showNotificationPreferencesDialog();
//        });

        // Help Section Click Listener (Popup with Help Message)
        rootView.findViewById(R.id.help_layout).setOnClickListener(v -> {
            showHelpDialog();
        });

        // About Section Click Listener (Navigate to About Page)
        rootView.findViewById(R.id.about_layout).setOnClickListener(v -> {
            showAboutDialog(); // Show the About dialog
        });

        // Set the current theme mode when the fragment is created
        applySavedThemeMode();

        return rootView;
    }

    // Show Account Info Dialog with Validation
    public void showAccountInfoDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_info, null);
        EditText editName = dialogView.findViewById(R.id.name_edit_text);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Account Information")
                .setView(dialogView)
                .setCancelable(true);

        final AlertDialog dialog = builder.create();

        // Save button logic with validation
        saveButton.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();

            // Check if the input is empty
            if (newName.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a name.", Toast.LENGTH_SHORT).show();
            }
            // Check if the input contains only valid characters (letters and spaces)
            else if (!newName.matches("[a-zA-Z\\s]+")) {
                Toast.makeText(getActivity(), "Name can only contain letters and spaces.", Toast.LENGTH_SHORT).show();
            }
            // If the input is valid, save it to SharedPreferences
            else {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("username", newName);
                editor.apply();
                Toast.makeText(getActivity(), "Name changed to: " + newName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Show Notification Preferences Dialog (Enable Push Notifications)
    private void showNotificationPreferencesDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notification_preferences, null);
        Switch pushNotificationsToggle = dialogView.findViewById(R.id.push_notifications_toggle);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Notification Preferences")
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Save", (dialog, which) -> {
                    boolean notificationsEnabled = pushNotificationsToggle.isChecked();
                    // Save the notification preference in SharedPreferences
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("push_notifications_enabled", notificationsEnabled);
                    editor.apply();
                    Toast.makeText(getActivity(), "Notifications " + (notificationsEnabled ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Show Help Dialog
    private void showHelpDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_help, null);

        // Create the AlertDialog with the custom layout
        new AlertDialog.Builder(getActivity())
                .setView(dialogView) // Set the custom layout view
                .setTitle("Help") // Title of the dialog
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // Button to dismiss the dialog
                .setCancelable(false) // Prevent closing the dialog by tapping outside
                .create()
                .show();
    }

    // Show About Dialog
    private void showAboutDialog() {
        // Inflate the custom dialog layout (use the dialog_about.xml)
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_version_history, null);

        // Set the version information dynamically
        TextView versionText = dialogView.findViewById(R.id.versionTitle);
        String versionStr = "Version Name: " + BuildConfig.VERSION_NAME + "\n" +
                "Version Code: " + BuildConfig.VERSION_CODE + "\n\n" +
                "Authors:\n" +
                "JP Marques, Ethan D'Mello, Evan Ellig, Zorah Jawadi, Arhan Ikram";
        versionText.setText(versionStr);

        // Create the AlertDialog with the custom layout
        new AlertDialog.Builder(getActivity())
                .setView(dialogView) // Set the custom layout view
                .setTitle("About") // Title of the dialog
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // Button to dismiss the dialog
                .setCancelable(false) // Prevent closing the dialog by tapping outside
                .create()
                .show();
    }

    // Apply the saved theme from SharedPreferences
    public void applySavedThemeMode() {
        // Retrieve the saved theme preference
        int themeMode = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);

        // Apply the saved theme
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
}

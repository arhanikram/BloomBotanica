package com.example.bloombotanica.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.bloombotanica.R;

public class AboutPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        // You could also use this if the dialog is within an activity layout: View dialogView = inflater.inflate(R.layout.dialog_help, null);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_help, null);

        // Set up the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // "OK" button closes the dialog
                .setCancelable(false); // Prevents the dialog from being dismissed by tapping outside

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Apply edge-to-edge padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(dialogView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

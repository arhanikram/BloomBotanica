package com.example.bloombotanica.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.ui.LogDetailActivity;

public class DeleteImageDialog {

    public static void showDeleteConfirmationDialog(Context context, int position, final LogDetailActivity activity) {
        // Create a new confirmation dialog
        new AlertDialog.Builder(context)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // On confirmation, delete the image from the list
                    activity.onImageDelete(position);  // Call the method to delete the image
                    Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}

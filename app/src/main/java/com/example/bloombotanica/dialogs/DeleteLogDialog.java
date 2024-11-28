package com.example.bloombotanica.dialogs;

import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.JournalEntry;

public class DeleteLogDialog {

    private static UserPlantDatabase userpdb;

    public interface DeleteLogListener {
        void onDeleteComplete(int position);
    }

    public static void showDeleteConfirmationDialog(Context context, UserPlantDatabase database, JournalEntry logToDelete, int position, DeleteLogListener listener) {

        userpdb = UserPlantDatabase.getInstance(context);

        new AlertDialog.Builder(context)
                .setTitle("Delete Log")
                .setMessage("Are you sure you want to delete this log?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Execute the deletion in a background thread
                    new Thread(() -> {
                        // Delete the log entry from the database
                        database.journalEntryDao().delete(logToDelete);

                        // Notify on the main thread that deletion is complete
                        if (listener != null) {
                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                listener.onDeleteComplete(position);
                                Toast.makeText(context, "Log deleted", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("No", null)
                .show();
    }
}

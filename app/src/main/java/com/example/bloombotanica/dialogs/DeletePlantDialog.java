package com.example.bloombotanica.dialogs;

import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.UserPlant;

public class DeletePlantDialog {

    private static UserPlantDatabase userpdb;

    public interface DeletePlantListener {
        void onDeleteComplete(int position);
    }

    public static void showDeleteConfirmationDialog(Context context, UserPlantDatabase database, UserPlant plantToDelete, int position, DeletePlantListener listener) {

        userpdb = UserPlantDatabase.getInstance(context);

        new AlertDialog.Builder(context)
                .setTitle("Delete Plant")
                .setMessage("Are you sure you want to delete this plant?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Execute the deletion in a background thread
                    new Thread(() -> {
                        database.userPlantDao().delete(plantToDelete);
                        // Notify on the main thread that deletion is complete
                        if (listener != null) {
                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                listener.onDeleteComplete(position);
                                Toast.makeText(context, "Plant deleted", Toast.LENGTH_SHORT).show();
                            });
                        }
                        userpdb.taskDao().removeTasksForDeletedPlants();
                    }).start();
                })
                .setNegativeButton("No", null)
                .show();
    }
}

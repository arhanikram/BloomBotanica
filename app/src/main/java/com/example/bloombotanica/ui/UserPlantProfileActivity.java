package com.example.bloombotanica.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Delete;

import com.example.bloombotanica.R;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.dialogs.DeletePlantDialog;
import com.example.bloombotanica.models.UserPlant;

public class UserPlantProfileActivity extends AppCompatActivity implements DeletePlantDialog.DeletePlantListener {

    private UserPlantDatabase userPlantDatabase;
    private int userPlantId;
    private TextView plantNickname;
    private UserPlant userPlant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_plant_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        plantNickname = findViewById(R.id.userplant_nickname);

        userPlantDatabase = UserPlantDatabase.getInstance(this);

        userPlantId = getIntent().getIntExtra("userPlantId", -1);
        if (userPlantId != -1) {
            fetchUserPlant();

        } else {
            Toast.makeText(this, "Invalid user plant ID", Toast.LENGTH_SHORT).show();
            finish();

        }


        Toolbar toolbar = findViewById(R.id.user_plant_profile_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }

    private void fetchUserPlant() {
        new Thread(() -> {
            userPlant = userPlantDatabase.userPlantDao().getUserPlantById(userPlantId);
            runOnUiThread(() -> {
                if (userPlant != null) {
                    plantNickname.setText(userPlant.getNickname());

                } else {
                    Toast.makeText(UserPlantProfileActivity.this, "User plant not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_plant_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit_user_plant) {
            // Handle edit user plant action
            Toast.makeText(this, "Edit User Plant", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.delete_user_plant) {
            showDeleteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        DeletePlantDialog.showDeleteConfirmationDialog(this, userPlantDatabase, userPlant, 0, this);
    }

    @Override
    public void onDeleteComplete(int position) {
        // Handle what to do after deletion, like finishing the activity
        Toast.makeText(this, "Plant deleted successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity after deletion
    }
}
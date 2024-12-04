package com.example.bloombotanica.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


import com.example.bloombotanica.R;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.dialogs.DeletePlantDialog;
import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.utils.DateUtils;
import com.example.bloombotanica.utils.TaskNotificationWorker;
import com.example.bloombotanica.utils.TaskUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UserPlantProfileActivity extends AppCompatActivity implements DeletePlantDialog.DeletePlantListener {

    private UserPlantDatabase userPlantDatabase;
    private PlantCareDatabase plantCaredb;
    private int userPlantId;
    public TextView plantNickname;
    public TextView plantName;
    public TextView plantSciName;
    private TextView lastWateredText;
    private TextView plant_description;
    private TextView lastTurnedText;
    private UserPlant userPlant;
    private PlantCare plantCare;
    private ImageView plantImageView;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private ProgressBar waterProgress;
    public ProgressBar turnProgress;
    private ImageButton waterButton, turnButton;
    private Button journalButton;
    private TaskDao taskDao;
    private Task task, turnTask, waterTask, memTask;

    private boolean TESTING = false; //SET TO TRUE FOR TESTING INSTANT NOTIFICATIONS

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

        Log.d("UserPlantProfileActivity", "onCreate called");
        Log.d("UserPlantProfileActivity", "UserPlantId: " + userPlantId);

        taskDao = UserPlantDatabase.getInstance(this).taskDao();

        plantNickname = findViewById(R.id.userplant_nickname);
        plantName = findViewById(R.id.plant_common_name);
        plantSciName = findViewById(R.id.plant_sci_name);
        plant_description = findViewById(R.id.plant_description);

        waterProgress = findViewById(R.id.water_progress);
        waterButton = findViewById(R.id.water_button);
        lastWateredText = findViewById(R.id.last_watered_text);
        turnButton = findViewById(R.id.rotate_button);
        turnProgress = findViewById(R.id.rotate_progress);
        lastTurnedText = findViewById(R.id.last_rotated_text);
        journalButton = findViewById(R.id.journal_button);

        userPlantDatabase = UserPlantDatabase.getInstance(this);
        plantCaredb = PlantCareDatabase.getInstance(this);

        userPlantId = getIntent().getIntExtra("userPlantId", -1);
        if (userPlantId != -1) {
            fetchUserPlant();

        } else {
            Toast.makeText(this, "Invalid user plant ID", Toast.LENGTH_SHORT).show();
            finish();

        }


        Toolbar toolbar = findViewById(R.id.user_plant_profile_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        plantImageView = findViewById(R.id.user_plant_profile_img);
        plantImageView.setOnClickListener(v -> showImageSourceDialog());
        waterButton.setOnClickListener(v -> {
            if (userPlant != null) {
                markPlantAsWatered(userPlant);
            } else {
                Toast.makeText(this, "Plant not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        turnButton.setOnClickListener(v -> {
            if (userPlant != null) {
                markPlantAsTurned(userPlant);
            } else {
                Toast.makeText(this, "Plant not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        journalButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, JournalActivity.class);
            intent.putExtra("plantId", userPlantId);
            startActivity(intent);
        });

        try {
            Log.d("!!!!!", userPlant.getImagePath());
        } catch (Exception e) {
            Log.d("!!!!!", "null");
        }
    }

    private void markPlantAsTurned(UserPlant userPlant) {
        Log.d("markPlantAsTurned", "Marking plant as turned");
        // Use TaskUtils.renewTask to handle task completion and plant turning updates
        new Thread(() -> {
            try {
                int turningFrequency = plantCaredb.plantCareDao().getTurningFrequencyById(userPlant.getPlantCareId());
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.add(Calendar.DAY_OF_YEAR, turningFrequency);
                Date nextTurningDate = calendar.getTime();

                //first time turning plant
                if (userPlant.getLastTurned() == null) {

                    // Update in-memory userPlant object
                    userPlant.setLastTurned(today);
                    userPlant.setNextTurningDate(nextTurningDate);
//                    userPlant.setTurned(true);
                    userPlantDatabase.userPlantDao().updateTurningDates(userPlantId, today, nextTurningDate);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Plant turned successfully!", Toast.LENGTH_SHORT).show();
                        Log.d("markPlantAsTurned", "Next Turning Date: " + nextTurningDate);
                        updateTurnProgress(turnTask.isOverdue());
                        lastTurnedText.setText(R.string.last_turned_today);
                    });

//                    JournalEntry entry = new JournalEntry();
//                    entry.setPlantId(userPlant.getId());
//                    entry.setTimestamp(today);
//                    entry.setTitle("Turned");
//                    userPlantDatabase.journalEntryDao().insert(entry);

                    TaskDao taskDao = UserPlantDatabase.getInstance(this).taskDao();
                    UserPlantDao userPlantDao = UserPlantDatabase.getInstance(this).userPlantDao();
                    PlantCareDatabase plantCareDatabase = PlantCareDatabase.getInstance(this);

                    task = taskDao.getTaskForUserPlantAndType(userPlant.getId(), "Rotate");
                    Log.d("markPlantAsTurned", "currently turning: " + task.toString());

                    TaskUtils.renewTask(task, taskDao, userPlantDao, plantCareDatabase, () -> {
                        runOnUiThread(() -> {
                            // Update UI to reflect changes
//                            updateTurnProgress(task.isOverdue()); // Update the turning progress bar
                            lastTurnedText.setText(R.string.last_turned_today); // Update "last turned" text
                            Toast.makeText(this, "Plant turned successfully!", Toast.LENGTH_SHORT).show();
                        });
                        task = taskDao.getLatestTask();
                        Log.d("markPlantAsTurned", "currently turning: " + task.toString());
                        requestNotificationPermissionAndSchedule(task);
                    });


                    Log.d("markPlantAsTurned", "First time turning plant");
                } else {
                    Calendar currentDate = Calendar.getInstance();
                    int currentDay = currentDate.get(Calendar.DAY_OF_YEAR);
                    int currentYear = currentDate.get(Calendar.YEAR);

                    Date lastTurned = userPlant.getLastTurned();
                    currentDate.setTime(lastTurned);
                    int lastTurnedDay = currentDate.get(Calendar.DAY_OF_YEAR);
                    int lastTurnedYear = currentDate.get(Calendar.YEAR);

                    if (currentDay == lastTurnedDay && currentYear == lastTurnedYear) {
                        //if plant already turned today
                        Log.d("markPlantAsTurned", "Plant already turned today");
                        runOnUiThread(() -> Toast.makeText(this, "Plant already turned today", Toast.LENGTH_SHORT).show());
                        return;
                    } else {
                        Log.d("markPlantAsTurned", "Plant not turned today");
                        TaskDao taskDao = UserPlantDatabase.getInstance(this).taskDao();
                        UserPlantDao userPlantDao = UserPlantDatabase.getInstance(this).userPlantDao();
                        PlantCareDatabase plantCareDatabase = PlantCareDatabase.getInstance(this);

                        task = taskDao.getTaskForUserPlantAndType(userPlant.getId(), "Rotate");

                        Log.d("markPlantAsTurned", "currently turning: " + task.toString());

                        // Update in-memory userPlant object
                        userPlant.setLastTurned(today);
                        userPlant.setNextTurningDate(nextTurningDate);
//                        userPlant.setTurned(true);

                        TaskUtils.renewTask(task, taskDao, userPlantDao, plantCareDatabase, () -> {
                            runOnUiThread(() -> {
                                // Update UI to reflect changes
                                updateTurnProgress(task.isOverdue()); // Update the turning progress bar
                                lastTurnedText.setText(R.string.last_turned_today); // Update "last turned" text
                                Toast.makeText(this, "Plant turned successfully!", Toast.LENGTH_SHORT).show();
                            });
                        });
                        task = taskDao.getLatestTask();
                        requestNotificationPermissionAndSchedule(task);
                    }
                }
            } catch (Exception e) {
                Log.e("markPlantAsTurned", "Error while marking plant as turned", e);
            }
        }).start();

    }

    private void updateTurnProgress(boolean isOverdue) {

        if (userPlant.getLastTurned() == null) {
            Log.d("updateTurnProgress", "Last turned date is null");
            return;
        }
        new Thread(() -> {
            if (isOverdue) {
                runOnUiThread(() -> turnProgress.setProgress(100));
                return;
            }

            if (userPlant != null) {
                Log.d("updateTurnProgress", "Updating progress bar");
                try {
                    Date today = new Date();
                    Date nextTurningDate = userPlant.getNextTurningDate();

                    // Calculate progress
                    long totalDuration = nextTurningDate.getTime() - userPlant.getLastTurned().getTime();
                    long remainingDuration = nextTurningDate.getTime() - today.getTime();
                    int progress = (int) (100 - (remainingDuration * 100 / totalDuration));

                    // Ensure progress is between 0 and 100
                    progress = Math.max(0, Math.min(progress, 100));

                    // Update progress bar on main thread
                    int finalProgress = progress;
                    runOnUiThread(() -> turnProgress.setProgress(finalProgress));
                    Log.d("updateTurnProgress", "Progress updated to " + finalProgress);
                } catch (Exception e) {
                    Log.e("updateTurnProgress", "Error updating progress bar", e);
                }
            }
        }).start();

    }

    private void fetchUserPlant() {
        Log.d("UserPlantProfileActivity", "Fetching user plant with ID: " + userPlantId);
        new Thread(() -> {
            userPlant = userPlantDatabase.userPlantDao().getUserPlantById(userPlantId);
            // Get plant name from plant care database
            plantCare = plantCaredb.plantCareDao().getPlantCareById(userPlant.getPlantCareId());

            runOnUiThread(() -> {
                if (userPlant != null) {
                    Log.d("UserPlantProfileActivity", "User plant found: " + userPlant.getNickname());
                    plantNickname.setText(userPlant.getNickname());
                    plantName.setText(plantCare.getCommonName());
                    plantSciName.setText(plantCare.getScientificName());
                    plant_description.setText(plantCare.getPlantDescription());
                    new Thread(() -> {
                        // Fetch the tasks on the background thread
                        waterTask = taskDao.getTaskForUserPlantAndType(userPlant.getId(), "Water");
                        turnTask = taskDao.getTaskForUserPlantAndType(userPlant.getId(), "Rotate");

                        // Now, update the UI after the tasks are fetched
                        runOnUiThread(() -> {
                            if (waterTask != null) {
                                int daysSinceLastWatered = calculateDaysSinceLastWatered(userPlant.getLastWatered());
                                if (daysSinceLastWatered == 0 && !waterTask.isOverdue()) {
                                    if (userPlant.getLastWatered() == null) {
                                        lastWateredText.setText(R.string.never_watered);
                                    } else {
                                        lastWateredText.setText(R.string.last_watered_today);
                                    }
                                } else if (daysSinceLastWatered == 1) {
                                    lastWateredText.setText(R.string.last_watered_yesterday);
                                } else if (waterTask.isOverdue()) {
                                    int overdue = calculateDaysOverdue(waterTask);
                                    lastWateredText.setText(getString(R.string.due_1_d_days_ago, overdue));
                                } else {
                                    lastWateredText.setText(getString(R.string.last_watered, daysSinceLastWatered));
                                }
                            } else {
                                lastWateredText.setText(""); // Handle null case
                                Log.d("UserPlantProfileActivity", "Water task is null");
                            }

                            if (turnTask != null) {
                                int daysSinceLastTurned = calculateDaysSinceLastTurned(userPlant.getLastTurned());
                                if (daysSinceLastTurned == 0 && !turnTask.isOverdue()) {
                                    if (userPlant.getLastTurned() == null) {
                                        lastTurnedText.setText(R.string.plant_added_turn_it);
                                    } else {
                                        lastTurnedText.setText(R.string.last_turned_today);
                                    }
                                } else if (daysSinceLastTurned == 1) {
                                    lastTurnedText.setText(R.string.last_turned_yesterday);
                                } else if (turnTask.isOverdue()) {
                                    int overdue = calculateDaysOverdue(turnTask);
                                    lastTurnedText.setText(getString(R.string.due_1_d_days_ago, overdue));
                                } else {
                                    lastTurnedText.setText(getString(R.string.last_turned, daysSinceLastTurned));
                                }
                            } else {
                                lastTurnedText.setText(""); // Handle null case
                                Log.d("UserPlantProfileActivity", "Turn task is null");
                            }

                            // Update progress indicators, checking for overdue status
                            updateWaterProgress(waterTask != null && waterTask.isOverdue());
                            updateTurnProgress(turnTask != null && turnTask.isOverdue());
                        });
                    }).start();


                    // Check if the user has uploaded an image (from internal storage)
                    if (userPlant.getImagePath() != null) {
                        Log.d("UserPlantProfileActivity", "Loading image from path: " + userPlant.getImagePath());
                        Bitmap bitmap = BitmapFactory.decodeFile(userPlant.getImagePath());
                        plantImageView.setImageBitmap(bitmap);
                    } else {
                        Log.d("UserPlantProfileActivity", "No image path found for user plant");

                        // If no image exists in internal storage, fall back to the default image based on the userPlantId
                        String imageResourceName = "a" + plantCare.getId();  // Add "a" before the ID (e.g., "a1" for plant ID 1)
                        int imageResId = getResources().getIdentifier(imageResourceName, "drawable", getPackageName());
                        Log.d("TESTID", String.valueOf(plantCare.getId()));
                        if (imageResId != 0) {
                            // Load the default image from drawable based on ID (e.g., "a1" for plant ID 1)
                            plantImageView.setImageResource(imageResId);
                        } else {
                            // Use a fallback default image (e.g., "a_default" as the name of the fallback image)
                            plantImageView.setImageResource(R.drawable.default_plant_image);
                        }
                    }

                } else {
                    Log.d("UserPlantProfileActivity", "User plant not found");
                    Toast.makeText(UserPlantProfileActivity.this, "User plant not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    private int calculateDaysSinceLastTurned(Date lastTurned) {

        //if plant has never been watered (just added plant)
        if (lastTurned == null) {
            Log.d("calculateDaysSinceLastTurned", "Last turned date is null");
            return 0;
        }

        Log.d("calculateDaysSinceLastTurned", "Calculating days since last turned");
        Date today = new Date();
        long differenceInMillis = today.getTime() - lastTurned.getTime();
        return (int) (differenceInMillis / (1000 * 60 * 60 * 24)); // Convert milliseconds to days
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

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(new String[]{"Take a Picture", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        // Take a picture
                        openCamera();
                    } else if (which == 1) {
                        // Choose from gallery
                        openImagePicker();
                    }
                })
                .show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); // Only show images
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void openCamera() {
        // Check if the CAMERA permission is granted
        Log.d("UserPlantProfileActivity", "openCamera: Checking camera permission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the CAMERA permission
            Log.d("UserPlantProfileActivity", "openCamera: Requesting camera permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            // Permission already granted, proceed to open the camera
            Log.d("UserPlantProfileActivity", "openCamera: Permission granted, launching camera");
            launchCamera();
        }
    }

    // Launch the camera intent
    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                Log.d("UserPlantProfileActivity", "onRequestPermissionsResult: Permission granted, launching camera");
                launchCamera();
            } else {
                // Permission denied, show a message to the user
                Log.d("UserPlantProfileActivity", "onRequestPermissionsResult: Permission denied");
                Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule the notification
                scheduleTaskNotification(memTask, TESTING);
            } else {
                // Permission denied, show a toast
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            String filename = "plant_image_" + userPlantId + ".png";
            Log.d("UserPlantProfileActivity", "saveImageToInternalStorage: Saving image as " + filename);
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            String filePath = new File(getFilesDir(), filename).getAbsolutePath();
            Log.d("UserPlantProfileActivity", "saveImageToInternalStorage: Image saved at " + filePath);
            return filePath;
        } catch (Exception e) {
            Log.e("UserPlantProfileActivity", "saveImageToInternalStorage: Error saving image", e);
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                Log.d("UserPlantProfileActivity", "onActivityResult: Image picked from gallery");
                Uri selectedImageUri = data.getData();
                handleImageResult(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.d("UserPlantProfileActivity", "onActivityResult: Photo captured from camera");
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    Log.d("UserPlantProfileActivity", "onActivityResult: Saving captured photo");
                    String imagePath = saveImageToInternalStorage(photo);
                    if (imagePath != null) {
                        Log.d("UserPlantProfileActivity", "onActivityResult: Photo saved at " + imagePath);
                        plantImageView.setImageBitmap(photo);
                        saveImagePathToDatabase(imagePath);
                    } else {
                        Log.e("UserPlantProfileActivity", "onActivityResult: Failed to save captured photo");
                    }
                }
            }
        } else {
            Log.e("UserPlantProfileActivity", "onActivityResult: Result not OK or data is null");
        }
    }

    private void handleImageResult(Uri imageUri) {
        try {
            Log.d("UserPlantProfileActivity", "handleImageResult: Loading image from URI " + imageUri);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            String imagePath = saveImageToInternalStorage(bitmap);
            if (imagePath != null) {
                Log.d("UserPlantProfileActivity", "handleImageResult: Image saved at " + imagePath);
                plantImageView.setImageBitmap(bitmap);
                saveImagePathToDatabase(imagePath);
            } else {
                Log.e("UserPlantProfileActivity", "handleImageResult: Failed to save image");
            }
        } catch (Exception e) {
            Log.e("UserPlantProfileActivity", "handleImageResult: Error loading image", e);
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImagePathToDatabase(String imagePath) {
        new Thread(() -> userPlantDatabase.userPlantDao().updateImagePath(userPlantId, imagePath)).start();
    }

    //looks messy but it works
    private void markPlantAsWatered(UserPlant userPlant) {
        Log.d("markPlantAsWatered", "Marking plant as watered");
        // Use TaskUtils.renewTask to handle task completion and plant watering updates
        new Thread(() -> {
            try {
                int wateringFrequency = plantCaredb.plantCareDao().getWateringFrequencyById(userPlant.getPlantCareId());
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.add(Calendar.DAY_OF_YEAR, wateringFrequency);
                Date nextWateringDate = calendar.getTime();

                //first time watering plant
                if (userPlant.getLastWatered() == null) {

                    // Update in-memory userPlant object
                    userPlant.setLastWatered(today);
                    userPlant.setNextWateringDate(nextWateringDate);
//                    userPlant.setWatered(true);
                    userPlantDatabase.userPlantDao().updateWateringDates(userPlantId, today, nextWateringDate);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Plant watered successfully!", Toast.LENGTH_SHORT).show();
                        Log.d("markPlantAsWatered", "Next Watering Date: " + nextWateringDate);
                        updateWaterProgress(waterTask.isOverdue());
                        lastWateredText.setText(R.string.last_watered_today);
                    });

//                    JournalEntry entry = new JournalEntry();
//                    entry.setPlantId(plant.getId());
//                    entry.setTimestamp(today);
//                    entry.setTitle("Watered");
//                    userPlantDatabase.journalEntryDao().insert(entry);

                    TaskDao taskDao = UserPlantDatabase.getInstance(this).taskDao();
                    UserPlantDao userPlantDao = UserPlantDatabase.getInstance(this).userPlantDao();
                    PlantCareDatabase plantCareDatabase = PlantCareDatabase.getInstance(this);

                    task = taskDao.getTaskForUserPlantAndType(userPlant.getId(), "Water");

                    TaskUtils.renewTask(task, taskDao, userPlantDao, plantCareDatabase, () -> {
                        runOnUiThread(() -> {
                            // Update UI to reflect changes
                            updateWaterProgress(task.isOverdue()); // Update the watering progress bar
                            lastWateredText.setText(R.string.last_watered_today); // Update "last watered" text
                            Toast.makeText(this, "Plant watered for the first time successfully!", Toast.LENGTH_SHORT).show();
                        });
                    });

                    task = taskDao.getLatestTask();
                    Log.d("markPlantAsWatered", "task: " + task.toString());
                    requestNotificationPermissionAndSchedule(task);

                    Log.d("markPlantAsWatered", "First time watering plant");
                } else {
                    Calendar currentDate = Calendar.getInstance();
                    int currentDay = currentDate.get(Calendar.DAY_OF_YEAR);
                    int currentYear = currentDate.get(Calendar.YEAR);

                    Date lastWatered = userPlant.getLastWatered();
                    currentDate.setTime(lastWatered);
                    int lastWateredDay = currentDate.get(Calendar.DAY_OF_YEAR);
                    int lastWateredYear = currentDate.get(Calendar.YEAR);

                    if (currentDay == lastWateredDay && currentYear == lastWateredYear) {
                        //if plant already watered today
                        Log.d("markPlantAsWatered", "Plant already watered today");
                        runOnUiThread(() -> Toast.makeText(this, "Plant already watered today", Toast.LENGTH_SHORT).show());
                        return;
                    } else {
                        Log.d("markPlantAsWatered", "Plant not watered today");
                        TaskDao taskDao = UserPlantDatabase.getInstance(this).taskDao();
                        UserPlantDao userPlantDao = UserPlantDatabase.getInstance(this).userPlantDao();
                        PlantCareDatabase plantCareDatabase = PlantCareDatabase.getInstance(this);

                        task = taskDao.getTaskForUserPlantAndType(userPlant.getId(), "Water");

                        Log.d("markPlantAsWatered", "currently watering: " + task.toString());

                        // Update in-memory userPlant object
                        userPlant.setLastWatered(today);
                        userPlant.setNextWateringDate(nextWateringDate);
                        userPlant.setWatered(true);

                        TaskUtils.renewTask(task, taskDao, userPlantDao, plantCareDatabase, () -> {
                            runOnUiThread(() -> {
                                // Update UI to reflect changes
                                updateWaterProgress(false); // Update the watering progress bar
                                lastWateredText.setText(R.string.last_watered_today); // Update "last watered" text
                                Toast.makeText(this, "Plant watered successfully!", Toast.LENGTH_SHORT).show();
                            });
                            task = taskDao.getLatestTask();
                            requestNotificationPermissionAndSchedule(task);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("markPlantAsWatered", "Error while marking plant as watered", e);
            }
        }).start();
    }


    private void updateWaterProgress(boolean isOverdue) {
        if (userPlant.getLastWatered() == null) {
            Log.d("updateWaterProgress", "Last watered date is null");
            return;
        }
        new Thread(() -> {
            if (isOverdue) {
                runOnUiThread(() -> waterProgress.setProgress(100));
                return;
            }

            if (userPlant != null) {
                Log.d("updateWaterProgress", "Updating progress bar");
                try {
                    Date today = new Date();
                    Date nextWateringDate = userPlant.getNextWateringDate();

                    // Calculate progress
                    long totalDuration = nextWateringDate.getTime() - userPlant.getLastWatered().getTime();
                    long remainingDuration = nextWateringDate.getTime() - today.getTime();
                    int progress = (int) (100 - (remainingDuration * 100 / totalDuration));

                    // Ensure progress is between 0 and 100
                    progress = Math.max(0, Math.min(progress, 100));

                    // Update progress bar on main thread
                    int finalProgress = progress;
                    runOnUiThread(() -> waterProgress.setProgress(finalProgress));
                    Log.d("updateWaterProgress", "Progress updated to " + finalProgress);
                } catch (Exception e) {
                    Log.e("updateWaterProgress", "Error updating progress bar", e);
                }
            }
        }).start();
    }

    private int calculateDaysSinceLastWatered(Date lastWatered) {

        //if plant has never been watered (just added plant)
        if (lastWatered == null) {
            Log.d("calculateDaysSinceLastWatered", "Last watered date is null");
            return 0;
        }

        Log.d("calculateDaysSinceLastWatered", "Calculating days since last watered");
        Date today = new Date();
        long differenceInMillis = today.getTime() - lastWatered.getTime();
        return (int) (differenceInMillis / (1000 * 60 * 60 * 24)); // Convert milliseconds to days
    }

    private int calculateDaysOverdue(Task task) {
        if (task == null || task.getDueDate() == null) {
            return 0; // If no task or no due date, return 0
        }

        Date today = new Date();
        long differenceInMillis = today.getTime() - task.getDueDate().getTime(); // Calculate time difference in milliseconds

        // Convert milliseconds to days
        long differenceInDays = differenceInMillis / (1000 * 60 * 60 * 24); // Convert from milliseconds to days

        return (int) differenceInDays;
    }

    private void scheduleTaskNotification(Task task, boolean forTesting) {
        Log.d("AddPlantDialogFragment", "scheduleTaskNotification called");

        // Create a Calendar instance for scheduling the notification
        Calendar calendar = Calendar.getInstance();
        if (forTesting) {
            calendar.setTime(new Date());  // Set to current date for testing
        } else {
            calendar.setTime(task.getDueDate());  // Set to task's due date
        }

        // Set the time to 12:00 PM (for consistency)
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long delayInMillis = forTesting ? 0 : calendar.getTimeInMillis() - System.currentTimeMillis();  // Delay until the due time

        // Prepare the input data for the worker
        Data inputData = new Data.Builder()
                .putInt("taskId", task.getId())
                .putString("taskType", task.getTaskType())
                .putLong("dueDateMillis", task.getDueDate().getTime())
                .build();

        // Create a OneTimeWorkRequest to trigger the notification
        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(TaskNotificationWorker.class)
                .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        // Enqueue the work request to WorkManager
        WorkManager.getInstance(this).enqueue(notificationWorkRequest);

        Log.d("TaskNotificationProfile", "Notification scheduled for task " + task.getId() + " at " + calendar.getTime());
    }

    private void requestNotificationPermissionAndSchedule(Task task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13 or higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule the notification
                scheduleTaskNotification(task, TESTING);
            } else {
                // Request permission if not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                runOnUiThread(() ->
                        Toast.makeText(this, "We need permission to send notifications for task reminders.", Toast.LENGTH_LONG).show());
            }
        } else {
            // For API levels below 33, schedule without permission check
            scheduleTaskNotification(task, TESTING);
        }
    }


}
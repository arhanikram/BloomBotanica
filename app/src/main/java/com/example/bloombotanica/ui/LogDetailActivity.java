package com.example.bloombotanica.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.ImageAdapter;
import com.example.bloombotanica.database.JournalEntryDao;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.JournalEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogDetailActivity extends AppCompatActivity {

    private static final int IMAGE_PICKER_REQUEST = 1;  // Gallery pick
    private static final int IMAGE_CAPTURE_REQUEST = 2; // Camera capture
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private List<String> imagePaths = new ArrayList<>();
    private EditText logTitle, logBody;
    private RecyclerView imagesRecyclerView;
    private FloatingActionButton addImageFab;
    private JournalEntry currentEntry;
    private JournalEntryDao journalEntryDao;
    private UserPlantDatabase userPlantDatabase;
    private int plantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logTitle = findViewById(R.id.log_title);
        logBody = findViewById(R.id.log_body);
        imagesRecyclerView = findViewById(R.id.images_recycler_view);

        addImageFab = findViewById(R.id.add_image_fab);

        userPlantDatabase = UserPlantDatabase.getInstance(this);
        journalEntryDao = userPlantDatabase.journalEntryDao();

        Toolbar toolbar = findViewById(R.id.log_detail_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            saveLog();
            getOnBackPressedDispatcher().onBackPressed();
        });

        plantId = getIntent().getIntExtra("plantId", -1);

        int entryId = getIntent().getIntExtra("entryId", -1);
        if(entryId != -1) {
            loadLog(entryId);
        } else {
            Log.d("LogDetailActivity", "Entry ID invalid");
        }

        addImageFab.setOnClickListener(v -> showImageSourceDialog());

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(new ImageAdapter(this, imagePaths, this::onImageDelete));
    }

    public void onImageDelete(int position) {
        imagePaths.remove(position);
        ((ImageAdapter) imagesRecyclerView.getAdapter()).updateImagePaths(imagePaths);

        new Thread(() -> {
            currentEntry.setImagePaths(new Gson().toJson(imagePaths));
            journalEntryDao.update(currentEntry);
        }).start();
    }

    private void loadLog(int entryId) {
        // Run the database query in a background thread
        new Thread(() -> {
            currentEntry = journalEntryDao.getEntryById(entryId);
            runOnUiThread(() -> {
                if (currentEntry != null) {
                    logTitle.setText(currentEntry.getTitle());
                    logBody.setText(currentEntry.getBody() != null ? currentEntry.getBody() : "");
                    imagePaths = new Gson().fromJson(currentEntry.getImagePaths(), new TypeToken<List<String>>() {}.getType());

                    if (imagePaths != null && !imagePaths.isEmpty()) {
                        ((ImageAdapter) imagesRecyclerView.getAdapter()).updateImagePaths(imagePaths);
                    }
                } else {
                    // Handle case if entry not found (optional)
//                    Toast.makeText(this, "Journal entry not found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void saveLog() {
        String title = logTitle.getText().toString();
        String body = logBody.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Serialize image paths into JSON string
        String imagePathsJson = new Gson().toJson(imagePaths);

        // Create or update the log entry in the database
        new Thread(() -> {
            Log.d("LogDetailActivity", "Updating existing log");
            currentEntry.setTitle(title);
            currentEntry.setBody(body);
            currentEntry.setPlantId(plantId);
//            currentEntry.setTimestamp(new Date()); //commented out so it doesnt update log timestamp - my opinion is that it should work this way (jp)
            currentEntry.setImagePaths(imagePathsJson);

            journalEntryDao.update(currentEntry);
            Intent intent = new Intent();
            intent.putExtra("entryId", currentEntry.getId());
            setResult(RESULT_OK, intent);

            runOnUiThread(() -> finish());  // Go back to the previous activity
        }).start();
    }

    private void showImageSourceDialog() {
        Log.d("LogDetailActivity", "Showing image source dialog");
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
        Log.d("LogDetailActivity", "Opening image picker");

        // Check for the appropriate permissions based on the Android version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API level 33), request READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                // Permission granted, proceed with opening the gallery
                openGallery();
            }
        }  else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        // Open the image picker (gallery)
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_REQUEST);
    }

    private void openCamera() {
        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("LogDetailActivity", "Requesting camera permission");
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Log.d("LogDetailActivity", "Launching camera");
            // If permission granted, launch camera
            launchCamera();
        }
    }

    private void launchCamera() {
        Log.d("LogDetailActivity", "Launching camera");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, IMAGE_CAPTURE_REQUEST);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("LogDetailActivity", "onActivityResult called");
        Log.d("LogDetailActivity", "Request Code: " + requestCode);
        Log.d("LogDetailActivity", "Result Code: " + resultCode);
        Log.d("LogDetailActivity", "Data is null: " + (data == null));

        if (imagePaths == null) {
            imagePaths = new ArrayList<>();
        }

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == IMAGE_PICKER_REQUEST) {
                Log.d("LogDetailActivity", "Image selected from gallery");
                Uri selectedImageUri = data.getData();
                String imagePath = getImagePathFromUri(selectedImageUri);
                imagePaths.add(imagePath);
                ((ImageAdapter) imagesRecyclerView.getAdapter()).updateImagePaths(imagePaths);
            } else if (requestCode == IMAGE_CAPTURE_REQUEST) {
                Log.d("LogDetailActivity", "Image captured from camera");
                // Image captured from camera
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    Log.d("LogDetailActivity", "Saving image to internal storage");
                    String imagePath = saveImageToInternalStorage(photo);
                    imagePaths.add(imagePath);
                    ((ImageAdapter) imagesRecyclerView.getAdapter()).updateImagePaths(imagePaths);

                }
            } else {
                Log.d("LogDetailActivity", "Unknown request code: " + requestCode);
            }
        } else {
            Log.d("LogDetailActivity", "Result not OK or data is null");
        }
    }

    private String getImagePathFromUri(Uri uri) {
        Log.d("LogDetailActivity", "getImagePathFromUri called");

        if (uri == null) {
            Log.e("LogDetailActivity", "URI is null");
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                // Use ContentResolver to get a file path that can be used
                String[] projection = {MediaStore.Images.Media.DATA};
                try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        return cursor.getString(columnIndex);
                    }
                }

                // Alternative: Copy file to app's internal storage if direct path fails
                return copyImageToInternalStorage(uri);
            } catch (Exception e) {
                Log.e("LogDetailActivity", "Error getting image path", e);
                return null;
            }
        } else {
            // Existing method for older Android versions
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Log.e("LogDetailActivity", "Error getting image path", e);
            }
            return null;
        }
    }

    private String copyImageToInternalStorage(Uri uri) {
        try {
            // Generate a unique filename
            String filename = "log_image_" + System.currentTimeMillis() + ".png";

            // Open input and output streams
            try (
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    FileOutputStream outputStream = openFileOutput(filename, MODE_PRIVATE)
            ) {
                // Copy the image
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Return the path of the copied file
                return new File(getFilesDir(), filename).getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("LogDetailActivity", "Error copying image", e);
            return null;
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            String filename = "log_image_" + System.currentTimeMillis() + ".png";
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.d("LogDetailActivity", "Image saved to: " + getFilesDir() + "/" + filename);
            return new File(getFilesDir(), filename).getAbsolutePath();
        } catch (Exception e) {
            Log.e("LogDetailActivity", "Error saving image", e);
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //annoying permissions stuff

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with the action (open gallery)
                    openGallery();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle case when no permission result is returned
                Log.e("LogDetailActivity", "Permission request failed, no grant results received.");
            }
        } else if(requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LogDetailActivity", "Camera permission granted");
                // Permission granted, proceed to open camera
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
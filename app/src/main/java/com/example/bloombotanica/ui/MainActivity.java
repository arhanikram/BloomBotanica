package com.example.bloombotanica.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.bloombotanica.R;
import com.example.bloombotanica.databinding.ActivityMainBinding;
import com.example.bloombotanica.dialogs.AddPlantNicknameDialog;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.utils.CameraHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements AddPlantNicknameDialog.OnPlantAddedListener {

    ActivityMainBinding binding;
    private CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        cameraHelper = new CameraHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        }

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
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
            } else if(item.getItemId() == R.id.settings) {
                selectedFragment = new SettingsFragment();
            } else if(item.getItemId() == R.id.camera) {
                //open camera for ML model
                cameraHelper.showImageSourceDialog();

                //testing purposes
//                openPredictionActivityTEST();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        //set dashboard as default so it auto opens on start
        binding.bottomNavigationView.setSelectedItemId(R.id.dashboard);

        // Handle BottomNavigationView position dynamically
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationView, (view, insets) -> {
            // Get the system navigation bar height
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Offset the nav bar height to make it sit closer
            int offset = -24; // Move it 24dp closer to the bottom (negative value)

            // Adjust BottomNavigationView margin
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            params.bottomMargin = Math.max(0, navBarHeight + offset); // Ensure no negative margin
            view.setLayoutParams(params);

            return insets; // Return insets to retain other behaviors
        });
    }

    private void openPredictionActivityTEST() {
        Intent intent = new Intent(this, PredictionActivity.class);
        startActivity(intent);
    }

    // Called when an image is selected from the gallery
    public void onImageSelectedFromGallery(Uri imageUri) {
        // Handle image URI (e.g., display the image, save to internal storage, etc.)
        Log.d("MainActivity", "Image selected from gallery: " + imageUri.toString());

        Bitmap bitmap = getBitmapFromUri(imageUri);

        if (bitmap != null) {
            Log.d("MainActivity", "Image loaded successfully");
            String imagePath = saveImageToInternalStorage(bitmap);
            openPredictionActivity(imagePath);
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Failed to load image");
        }
    }

    // Called when an image is captured from the camera
    public void onImageCaptured(Bitmap photo) {
        // Handle the captured image (e.g., display the image, save to internal storage, etc.)
        Log.d("MainActivity", "Image captured from camera");

        String imagePath = saveImageToInternalStorage(photo);
        openPredictionActivity(imagePath);
    }

    // Convert Uri to Bitmap
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e("MainActivity", "Error decoding image from URI", e);
            return null;
        }
    }

    // Save the Bitmap image to internal storage and return the file path
    private String saveImageToInternalStorage(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("MainActivity", "Bitmap is null");
            return null;
        }
        try {
            String filename = "image_" + System.currentTimeMillis() + ".png";
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            String filePath = new File(getFilesDir(), filename).getAbsolutePath();
            Log.d("MainActivity", "Image saved to: " + filePath);
            return filePath;
        } catch (IOException e) {
            Log.e("MainActivity", "Error saving image to internal storage", e);
            return null;
        }
    }

    // Delegate result handling to CameraHelper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cameraHelper.handleActivityResult(requestCode, resultCode, data);  // Delegate result to CameraHelper
    }

    // Delegate permission result handling to CameraHelper
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);  // Delegate permission result to CameraHelper
    }

    // Open PredictionActivity and pass the captured image to it
    private void openPredictionActivity(String imagePath) {
        Log.d("MainActivity", "Opening PredictionActivity");
        if(imagePath != null) {
            Intent intent = new Intent(this, PredictionActivity.class);
            // You may want to convert the bitmap to a file path or save it temporarily.
            intent.putExtra("imagePath", imagePath);  // Pass the Bitmap to PredictionActivity
            startActivity(intent);
        } else {
            Log.d("MainActivity", "Image path is null");
        }
    }

    @Override
    public void onPlantAdded(UserPlant newPlant) {
        // Log to ensure the method is being called
        Log.d("MainActivity", "New plant added");

        // Create a new instance of PlantsFragment
        PlantsFragment plantsFragment = new PlantsFragment();

        // Replace the current fragment with PlantsFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, plantsFragment)
                .addToBackStack(null)  // Optional: add to back stack so user can navigate back
                .commit();
    }

}
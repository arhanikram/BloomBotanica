package com.example.bloombotanica.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bloombotanica.ui.MainActivity;

public class CameraHelper {
    private static final int IMAGE_CAPTURE_REQUEST = 1001;
    private static final int IMAGE_PICKER_REQUEST = 1002;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2002;

    private Activity activity;

    public CameraHelper(Activity activity) {
        this.activity = activity;
    }

    // Method to show the image source dialog
    public void showImageSourceDialog() {
        Log.d("CameraHelper", "Showing image source dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select Image Source")
                .setItems(new String[]{"Take a Picture", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openImagePicker();
                    }
                })
                .show();
    }

    // Method to open the image picker (gallery)
    private void openImagePicker() {
        Log.d("CameraHelper", "Opening image picker");

        // For Android 13+ (API level 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    // Open the gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, IMAGE_PICKER_REQUEST);
    }

    // Method to open the camera
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            launchCamera();
        }
    }

    // Launch the camera intent
    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, IMAGE_CAPTURE_REQUEST);
        } else {
            Toast.makeText(activity, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle permissions result (camera and gallery)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(activity, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(activity, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle the result of the activity (camera or gallery)
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICKER_REQUEST && data != null) {
                Uri selectedImageUri = data.getData();
                // Pass the URI to MainActivity for processing
                ((MainActivity) activity).onImageSelectedFromGallery(selectedImageUri);
            } else if (requestCode == IMAGE_CAPTURE_REQUEST && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    // Pass the bitmap to MainActivity for processing
                    ((MainActivity) activity).onImageCaptured(photo);
                }
            }
        }
    }
}

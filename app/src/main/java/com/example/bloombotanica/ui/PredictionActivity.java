package com.example.bloombotanica.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bloombotanica.R;
import com.example.bloombotanica.database.PlantCareDao;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.dialogs.AddPlantDialogFragment;
import com.example.bloombotanica.dialogs.AddPlantNicknameDialog;
import com.example.bloombotanica.ml.ModelHelper;
import com.example.bloombotanica.ml.PredictionResult;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.UserPlant;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class PredictionActivity extends AppCompatActivity implements AddPlantNicknameDialog.OnPlantAddedListener {

    private TextView predictionResultTextView, confidencePercentage, wateringInstruction, sunlightInstruction, soilInstruction;
    private ImageView imageView;
    private PlantCareDao plantCareDao;
    private PlantCare plantCare;
    private PlantCareDatabase plantCaredb;
    private int confidence;
    private String commonName, watering, sunlight, soil, imagePath;
    private ProgressBar progressBar;
    private Button addPlantButton;
    private PredictionResult result;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        predictionResultTextView = findViewById(R.id.ml_common_name_text);
        imageView = findViewById(R.id.ml_captured_img);
        progressBar = findViewById(R.id.confidence_progress_bar);
        confidencePercentage = findViewById(R.id.confidence_percentage);
        wateringInstruction = findViewById(R.id.watering_instruction);
        sunlightInstruction = findViewById(R.id.sunlight_instruction);
        soilInstruction = findViewById(R.id.soil_instruction);
        addPlantButton = findViewById(R.id.add_plant);

        imagePath = getIntent().getStringExtra("imagePath");
        Log.d("PredictionActivity", "Image path: " + imagePath);

//        Testing method
//        Bitmap bitmap = loadImageFromAssets("x.webp");

        if (imagePath != null) {
            // Display the image in ImageView
            bitmap = loadImageFromFile(imagePath);
            if(bitmap != null){
                imageView.setImageBitmap(bitmap);

                runModel(bitmap);
            } else {
                Log.d("PredictionActivity", "Bitmap is null");
            }

        } else {
            Log.d("PredictionActivity", "Image path is null");
        }

        addPlantButton.setOnClickListener(v -> {
            // Pass the PlantCare object to the dialog
            AddPlantNicknameDialog dialog = new AddPlantNicknameDialog();
            Bundle args = new Bundle();
            args.putSerializable("plantCare", plantCare);  // Pass the PlantCare object to the dialog
            args.putString("imagePath", imagePath);
            dialog.setArguments(args); // Set the arguments for the dialog
            dialog.show(getSupportFragmentManager(), "AddPlantNicknameDialog");
        });
    }

    @Override
    public void onPlantAdded(UserPlant newPlant) {
        // Create a new PlantsFragment
//        PlantsFragment plantsFragment = new PlantsFragment();
//
//        // Optionally pass the newly added plant's ID
//        Bundle args = new Bundle();
//        args.putInt("userPlantId", newPlant.getId());
//        plantsFragment.setArguments(args);
//
//        // Replace the current fragment with PlantsFragment
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, plantsFragment)
//                .addToBackStack(null)
//                .commit();

        newPlant.setImagePath(imagePath);
        Log.d("new plant image path" , newPlant.getImagePath());

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userPlantId", newPlant.getId());
        intent.putExtra("navToPlants", true);
        startActivity(intent);
    }


    private Bitmap loadImageFromFile(String imagePath) {
        try{
            FileInputStream inputStream = new FileInputStream(imagePath);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e("PredictionActivity", "Error loading image from file", e);
            return null; // Return null if image loading fails
        }
    }

    //test debug method
    private Bitmap loadImageFromAssets(String fileName) {
        try {
            // Open the image from assets folder
            InputStream inputStream = getAssets().open(fileName);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e("PredictionActivity", "Error loading image from assets", e);
            return null; // Return null if image loading fails
        }
    }

    private void runModel(Bitmap bitmap) {
        // Assuming you have a ModelHelper class to handle the ONNX model logic
        ModelHelper modelHelper = new ModelHelper(getApplicationContext());
        result = modelHelper.runModel(bitmap);  // Run the model and get the result
        Log.d("PredictionActivity", "Model result: " + result);
        // Use a background thread to query the database
        new Thread(() -> {
            plantCaredb = PlantCareDatabase.getInstance(this);
            plantCare = plantCaredb.plantCareDao().getPlantCareById(result.getPredictedClassIndex());

            // Check if plantCare is null to prevent NullPointerException
            if (plantCare != null) {
                commonName = plantCare.getCommonName();
                watering = "Water every " + plantCare.getWateringFrequency() + " days";
                sunlight = plantCare.getSunlight() + " is preferred";
                soil = "Use " + plantCare.getSoilType().toLowerCase();
                confidence = (int) (result.getConfidencePercentage() * 100);
            } else {
                commonName = "Unknown Plant"; // Fallback if no plant care data found
                confidence = 0;
            }

            confidence = Math.max(0, Math.min(confidence, 100));

            int finalConfidence = confidence;

            // Update the UI on the main thread
            runOnUiThread(() -> {
                predictionResultTextView.setText(commonName);
                wateringInstruction.setText(watering);
                sunlightInstruction.setText(sunlight);
                soilInstruction.setText(soil);
                // Format confidence as percentage (0-100) and set progress
                confidencePercentage.setText(String.format(Locale.getDefault(), "%d%%", finalConfidence));
                progressBar.setProgress(finalConfidence);  // Confidence as an integer (scaled to 100)
                //log progress bar progress
                Log.d("PredictionActivity", "Progress Bar Confidence: " + finalConfidence);
                //log plantcare id
            });

        }).start();
    }

}

package com.example.bloombotanica.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloombotanica.R;
import com.example.bloombotanica.ml.ModelHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PredictionActivity extends AppCompatActivity {

    private TextView predictionResultTextView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        predictionResultTextView = findViewById(R.id.ml_result_text);
        imageView = findViewById(R.id.ml_captured_img);

        String imagePath = getIntent().getStringExtra("imagePath");

//        Testing method
//        Bitmap bitmap = loadImageFromAssets("x.webp");

        if (imagePath != null) {
            // Display the image in ImageView
            Bitmap bitmap = loadImageFromFile(imagePath);
            if(bitmap != null){
                imageView.setImageBitmap(bitmap);

                runModel(bitmap);
            } else {
                Log.d("PredictionActivity", "Bitmap is null");
            }

        } else {
            Log.d("PredictionActivity", "Image path is null");
        }

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
        String result = modelHelper.runModel(bitmap);  // Run the model and get the result

        // Display the prediction result
        predictionResultTextView.setText(result);
    }
}

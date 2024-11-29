package com.example.bloombotanica.ml;

import ai.onnxruntime.*;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;

public class ModelHelper {

    private static final String MODEL_PATH = "plant_model.onnx";  // Name of the model file in assets
    private static final String[] CLASS_LABELS = {
            "African Violet (Saintpaulia ionantha)",
            "Aloe Vera", "Anthurium (Anthurium andraeanum)", "Areca Palm (Dypsis lutescens)",
            "Asparagus Fern (Asparagus setaceus)", "Begonia (Begonia spp.)", "Bird of Paradise (Strelitzia reginae)",
            "Birds Nest Fern (Asplenium nidus)", "Boston Fern (Nephrolepis exaltata)", "Calathea",
            "Cast Iron Plant (Aspidistra elatior)", "Chinese evergreen (Aglaonema)", "Chinese Money Plant (Pilea peperomioides)",
            "Christmas Cactus (Schlumbergera bridgesii)", "Chrysanthemum", "Ctenanthe", "Daffodils (Narcissus spp.)",
            "Dracaena", "Dumb Cane (Dieffenbachia spp.)", "Elephant Ear (Alocasia spp.)", "English Ivy (Hedera helix)",
            "Hyacinth (Hyacinthus orientalis)", "Iron Cross begonia (Begonia masoniana)", "Jade plant (Crassula ovata)",
            "Kalanchoe", "Lilium (Hemerocallis)", "Lily of the valley (Convallaria majalis)", "Money Tree (Pachira aquatica)",
            "Monstera Deliciosa (Monstera deliciosa)", "Orchid", "Parlor Palm (Chamaedorea elegans)", "Peace lily",
            "Poinsettia (Euphorbia pulcherrima)", "Polka Dot Plant (Hypoestes phyllostachya)", "Ponytail Palm (Beaucarnea recurvata)",
            "Pothos (Ivy arum)", "Prayer Plant (Maranta leuconeura)", "Rattlesnake Plant (Calathea lancifolia)",
            "Rubber Plant (Ficus elastica)", "Sago Palm (Cycas revoluta)", "Schefflera", "Snake plant (Sansevieria)",
            "Tradescantia", "Tulip", "Venus Flytrap", "Yucca", "ZZ Plant (Zamioculcas zamiifolia)"
    };

    private OrtEnvironment env;
    private OrtSession session;

    // Constructor: Initialize the ONNX runtime and load the model
    public ModelHelper(Context context) {
        Log.d("ModelHelper", "Initializing ONNX model...");
        try {
            // Initialize ONNX Runtime environment
            env = OrtEnvironment.getEnvironment();

            // Load model from assets using AssetManager
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(MODEL_PATH);
            byte[] modelBytes = new byte[inputStream.available()];
            inputStream.read(modelBytes);
            inputStream.close();

            // Create the ONNX session from the byte array of the model
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            session = env.createSession(modelBytes, options);

            Log.d("ModelHelper", "ONNX model loaded successfully.");
        } catch (IOException | OrtException e) {
            Log.e("ModelHelper", "Error loading ONNX model", e);
        }
    }

    public String runModel(Bitmap bitmap) {
        Log.d("ModelHelper", "Running ONNX model...");
        try {
            // Step 1: Preprocess the image
            float[] inputTensor = preprocessImage(bitmap);

            // Debugging: Log input tensor details
            Log.d("ModelHelper", "Input tensor first 20 values: " +
                    Arrays.toString(Arrays.copyOfRange(inputTensor, 0, Math.min(20, inputTensor.length))));
            Log.d("ModelHelper", "Input tensor length: " + inputTensor.length);

            // Step 2: Convert the input array to a FloatBuffer
            FloatBuffer buffer = FloatBuffer.wrap(inputTensor);

            // Step 3: Define the shape of the tensor (batch_size, channels, height, width)
            // Ensure this matches EXACTLY how the model was exported
            long[] shape = new long[]{1, 3, 224, 224};

            // Step 4: Create an ONNX tensor from the FloatBuffer and shape
            try (OnnxTensor input = OnnxTensor.createTensor(env, buffer, shape)) {
                // Step 5: Run inference
                try (OrtSession.Result result = session.run(Collections.singletonMap("input", input))) {
                    // Step 6: Get the output
                    Object resultValue = result.get(0).getValue();

                    // Detailed logging of output
                    if (resultValue instanceof float[][]) {
                        float[][] output = (float[][]) resultValue;
                        Log.d("ModelHelper", "Output shape: " + output.length + " x " + output[0].length);
                        Log.d("ModelHelper", "Raw logits first row: " + Arrays.toString(output[0]));
                        return processOutput(output[0]);
                    } else if (resultValue instanceof float[]) {
                        float[] output = (float[]) resultValue;
                        Log.d("ModelHelper", "Output length: " + output.length);
                        Log.d("ModelHelper", "Raw logits: " + Arrays.toString(output));
                        return processOutput(output);
                    } else {
                        Log.e("ModelHelper", "Unexpected output type: " +
                                (resultValue != null ? resultValue.getClass().getName() : "null"));
                        return "Unexpected output format";
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ModelHelper", "Comprehensive error in model inference", e);
            return "Error in inference: " + e.getMessage();
        }
    }

    private float[] preprocessImage(Bitmap bitmap) {
        Log.d("ModelHelper", "Preprocessing image...");

        // Step 1: Ensure image is in ARGB_8888 configuration
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Step 2: Resize the image to 224x224 (matching the model's expected input size)
        int inputSize = 224;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);

        // Step 3: Normalization constants (must match training preprocessing)
        float[] means = {0.485f, 0.456f, 0.406f};
        float[] stdDevs = {0.229f, 0.224f, 0.225f};

        // Step 4: Create a float array in NCHW format
        float[] floatArray = new float[3 * inputSize * inputSize];

        // Step 5: Extract and normalize pixel data
        for (int c = 0; c < 3; c++) {
            for (int h = 0; h < inputSize; h++) {
                for (int w = 0; w < inputSize; w++) {
                    // Calculate pixel index
                    int pixelIndex = h * inputSize + w;

                    // Get pixel from bitmap
                    int pixel = resizedBitmap.getPixel(w, h);

                    // Extract channel values and normalize
                    float channelValue;
                    if (c == 0) channelValue = ((pixel >> 16) & 0xFF) / 255.0f;  // Red
                    else if (c == 1) channelValue = ((pixel >> 8) & 0xFF) / 255.0f;  // Green
                    else channelValue = (pixel & 0xFF) / 255.0f;  // Blue

                    // Normalize the channel
                    channelValue = (channelValue - means[c]) / stdDevs[c];

                    // Store in NCHW format
                    floatArray[c * inputSize * inputSize + pixelIndex] = channelValue;
                }
            }
        }

        // Optional: Log first few normalized values for verification
        Log.d("ModelHelper", "Normalized values (first 10 per channel):");
        for (int c = 0; c < 3; c++) {
            StringBuilder channelLog = new StringBuilder("Channel " + c + ": ");
            for (int i = 0; i < 10; i++) {
                channelLog.append(String.format("%.4f ", floatArray[c * inputSize * inputSize + i]));
            }
            Log.d("ModelHelper", channelLog.toString());
        }

        return floatArray;
    }
    // Apply softmax to logits to convert them into probabilities
    private float[] applySoftmax(float[] logits) {
        float[] softmaxOutput = new float[logits.length];
        float sumExp = 0;
        for (float logit : logits) {
            sumExp += (float) Math.exp(logit);
        }
        for (int i = 0; i < logits.length; i++) {
            softmaxOutput[i] = (float) Math.exp(logits[i]) / sumExp;
        }
        return softmaxOutput;
    }

    // Process the output and get the predicted class
    private String processOutput(float[] output) {
        float[] probabilities = applySoftmax(output);
        int maxIndex = 0;
        float maxConfidence = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i];
                maxIndex = i;
            }
        }
        String predictedClass = CLASS_LABELS[maxIndex];
        return "Predicted Plant: " + predictedClass + " (Confidence: " + maxConfidence + ")";
    }

    // Release resources when done
    public void close() {
        Log.d("ModelHelper", "Closing ONNX session...");
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            Log.d("ModelHelper", "ONNX session closed.");
        } catch (OrtException e) {
            Log.e("ModelHelper", "Error closing ONNX session", e);
        }
    }
}
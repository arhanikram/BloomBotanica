package com.example.bloombotanica.ml;

import java.util.Locale;

public class PredictionResult {
    private int predictedClassIndex;
    private float confidencePercentage;

    public PredictionResult(int predictedClassIndex, float confidencePercentage) {
        this.predictedClassIndex = predictedClassIndex;
        this.confidencePercentage = confidencePercentage;
    }

    public int getPredictedClassIndex() {
        return predictedClassIndex;
    }

    public float getConfidencePercentage() {
        return confidencePercentage;
    }

    @Override
    public String toString() {
        return "Predicted Class: " + predictedClassIndex + " | Confidence: " + String.format(Locale.US, "%.2f", confidencePercentage) + "%";
    }
}

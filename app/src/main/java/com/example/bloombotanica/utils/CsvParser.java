package com.example.bloombotanica.utils;

import android.content.Context;
import android.util.Log;

import com.example.bloombotanica.models.PlantCare;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    public static List<PlantCare> parseCsv(Context context, String fileName) {
        List<PlantCare> plantCareList = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) { // Read each line
                String[] fields = line.split(",");
                String plantName = fields[0].trim();
                String scientificName = fields[1].trim();
                int wateringFrequencyDays = Integer.parseInt(fields[2].trim());
                String sunlight = fields[3].trim();
                String soilType = fields[4].trim();

                // Create a PlantCare object and add it to the list
                PlantCare plant = new PlantCare(plantName, scientificName, wateringFrequencyDays, sunlight, soilType);
                plantCareList.add(plant);
            }
            reader.close();
        } catch (Exception e) {
            Log.e("CsvParser", "Error parsing CSV: " + e.getMessage());
        }
        return plantCareList;
    }
}

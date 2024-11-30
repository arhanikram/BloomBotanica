package com.example.bloombotanica.utils;

import android.content.Context;
import android.util.Log;

import com.example.bloombotanica.models.PlantCare;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    public static List<PlantCare> parseCsv(Context context, String fileName) {
        List<PlantCare> plantCareList = new ArrayList<>();
        try {
            // Open the CSV file from the assets folder
            InputStream is = context.getAssets().open(fileName);
            InputStreamReader reader = new InputStreamReader(is);

            // CSVParser will automatically skip the header row
            CSVParser csvParser = new CSVParser(reader,
                    CSVFormat.DEFAULT.withFirstRecordAsHeader()
                            .withIgnoreHeaderCase()
                            .withTrim());

            // Loop through the records (skipping the header)
            for (CSVRecord record : csvParser) {
                try {
                    // Parse the fields from the CSV record
                    String plantName = record.get("Plant Name").trim();
                    String scientificName = record.get("Scientific Name").trim();
                    int wateringFrequencyDays = Integer.parseInt(record.get("Watering Frequency (days)").trim());
                    String sunlight = record.get("Sunlight").trim();
                    String soilType = record.get("Soil Type").trim();
                    String plantDescription = record.get("Plant Description").trim();

                    // Create a PlantCare object
                    PlantCare plant = new PlantCare(plantName, scientificName, wateringFrequencyDays, sunlight, soilType, plantDescription);
                    plantCareList.add(plant);
                } catch (Exception e) {
                    Log.e("CsvParser", "Error parsing record: " + e.getMessage());
                    // Log the problematic record for debugging
                    Log.e("CsvParser", "Problematic record: " + record.toString());
                }
            }

            // Close the CSV parser
            csvParser.close();
        } catch (Exception e) {
            Log.e("CsvParser", "Error reading the CSV file: " + e.getMessage());
        }
        return plantCareList;
    }
}

package com.example.bloombotanica.base;

import android.app.Application;
import android.util.Log;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.models.PlantCare;

import java.util.List;

public class BloomBotanicaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the database
        PlantCareDatabase plantCareDatabase = PlantCareDatabase.getInstance(getApplicationContext());

//        // Fetch and log all records in a background thread
//        new Thread(() -> {
//            List<PlantCare> plants = plantCareDatabase.plantCareDao().getAllPlants();
//            if (plants.isEmpty()) {
//                Log.d("DatabaseContent", "No records found in the PlantCare database.");
//            } else {
//                for (PlantCare plant : plants) {
//                    Log.d("DatabaseContent", "Plant: " + plant.getCommonName() +
//                            ", Scientific Name: " + plant.getScientificName() +
//                            ", Watering Frequency: " + plant.getWateringFrequency() +
//                            ", Sunlight: " + plant.getSunlight() +
//                            ", Soil Type: " + plant.getSoilType());
//                }
//            }
//        }).start();
    }

}

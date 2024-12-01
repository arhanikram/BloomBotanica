package com.example.bloombotanica.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.utils.CsvParser;
import com.example.bloombotanica.utils.Converters;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

@Database(entities = {PlantCare.class}, version = 4, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class PlantCareDatabase extends RoomDatabase {

    private static volatile PlantCareDatabase instance;

    public abstract PlantCareDao plantCareDao();

    public static synchronized PlantCareDatabase getInstance(Context context) {
        Log.d("PlantCareDatabase", "Getting the database instance" + context.toString());
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            PlantCareDatabase.class, "plant_care_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Log.d("PlantCareDatabase", "Database created.");
                            new Thread(() -> {
                                instance.populateDatabase(context);
                            }).start();
                        }

                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            Log.d("PlantCareDatabase", "Database opened.");
                            new Thread(() -> {
                                instance.checkForDatabaseUpdates(context);
                            }).start();
                        }
                    })
                    .build();
        }
        return instance;
    }

    private void checkForDatabaseUpdates(Context context) {
        Log.d("PlantCareDatabase", "Checking for database updates...");
        String currentFileHash = getFileHash(context, "plant_care.csv");
        String savedFileHash = getSavedFileHash(context);

        for (PlantCare plant : plantCareDao().getAllPlants()) {
            Log.d("PLANT IDS", plant.getId() + "  -  " + plant.getCommonName());
        }
        // If file hash is different, update the database
        if (!currentFileHash.equals(savedFileHash)) {
            Log.d("PlantCareDatabase", "Database file hash changed. Updating database...");
            populateDatabase(context);  // Re-populate the database
            saveFileHash(context, currentFileHash);  // Save new file hash to SharedPreferences
        } else {
            Log.d("PlantCareDatabase", "Database file hash is the same. No update needed.");
        }
    }

    // Calculate the hash of the file (e.g., MD5 or SHA)
    private String getFileHash(Context context, String fileName) {
        Log.d("PlantCareDatabase", "Calculating hash for file: " + fileName);
        try {
            InputStream is = context.getAssets().open(fileName);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] hashBytes = md.digest();
            return new BigInteger(1, hashBytes).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Get saved hash from SharedPreferences
    private String getSavedFileHash(Context context) {
        Log.d("PlantCareDatabase", "Getting saved file hash...");
        SharedPreferences prefs = context.getSharedPreferences("PlantCarePrefs", Context.MODE_PRIVATE);
        return prefs.getString("fileHash", "");
    }

    // Save new hash to SharedPreferences
    private void saveFileHash(Context context, String newHash) {
        Log.d("PlantCareDatabase", "Saving new file hash: " + newHash);
        SharedPreferences prefs = context.getSharedPreferences("PlantCarePrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("fileHash", newHash).apply();
    }

    private void populateDatabase(Context context) {
        Log.d("PlantCareDatabase", "Populating database...");
        List<PlantCare> plantCareList = CsvParser.parseCsv(context, "plant_care.csv");

        if (!plantCareList.isEmpty()) {
            Log.d("PlantCareDatabase", "Populating database with CSV data...");

            // First, add or update plants in the database
            for (PlantCare plant : plantCareList) {
                PlantCare existingPlant = plantCareDao().getByCommonName(plant.getCommonName());

                if (existingPlant == null) {
                    Log.d("PlantCareDatabase", "Inserting new plant: " + plant.getCommonName());
                    plantCareDao().insert(plant); // Insert new plant
                } else {
                    Log.d("PlantCareDatabase", "Updating existing plant: " + plant.getCommonName());
                    // Update existing plant (or insert, depending on your DAO logic)
                    plantCareDao().update(plant);
                }
            }

//            // Now, check for duplicates and remove them
//            List<PlantCare> allPlants = plantCareDao().getAllPlants();
//            Log.d("PlantCareDatabase", "Checking for duplicates...");
//            for (PlantCare plant : allPlants) {
//                List<PlantCare> duplicates = plantCareDao().searchByCommonName(plant.getCommonName());
//                if (duplicates.size() > 1) {
//                    Log.d("PlantCareDatabase", "Found duplicate plant: " + plant.getCommonName());
//                    // Keep the first one, and delete the rest
//                    for (int i = 1; i < duplicates.size(); i++) {
//                        Log.d("PlantCareDatabase", "Deleting duplicate plant: " + duplicates.get(i).getCommonName());
//                        plantCareDao().delete(duplicates.get(i)); // Assuming a delete method exists
//                    }
//                }
//            }

            // Now, remove plants that are no longer in the CSV
            for (PlantCare existingPlant : plantCareDao().getAllPlants()) {
                if (!isPlantInCsv(existingPlant, plantCareList)) {
                    Log.d("PlantCareDatabase", "Removing plant not in CSV: " + existingPlant.getCommonName());
                    plantCareDao().deleteByCommonName(existingPlant.getCommonName());
                }
            }

            Log.d("PlantCareDatabase", "Database populated and updated with CSV data.");
            for (PlantCare plant : plantCareDao().getAllPlants()) {
                Log.d("PlantCareDatabase", plant.getCommonName());
            }
        } else {
            Log.e("PlantCareDatabase", "CSV data is empty or failed to parse.");
        }
    }

    private boolean isPlantInCsv(PlantCare plant, List<PlantCare> plantCareList) {
        // Check if the plant exists in the CSV list (based on commonName)
        for (PlantCare csvPlant : plantCareList) {
            if (csvPlant.getCommonName() != null && csvPlant.getCommonName().equals(plant.getCommonName())) {
                return true;
            }
        }
        return false;
    }
}

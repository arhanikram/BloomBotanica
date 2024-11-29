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

@Database(entities = {PlantCare.class}, version = 1, exportSchema = false)
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
        String currentFileHash = getFileHash(context, "plant_care.csv");
        String savedFileHash = getSavedFileHash(context);

        // If file hash is different, update the database
        if (!currentFileHash.equals(savedFileHash)) {
            populateDatabase(context);  // Re-populate the database
            saveFileHash(context, currentFileHash);  // Save new file hash to SharedPreferences
        }
    }

    // Calculate the hash of the file (e.g., MD5 or SHA)
    private String getFileHash(Context context, String fileName) {
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
        SharedPreferences prefs = context.getSharedPreferences("PlantCarePrefs", Context.MODE_PRIVATE);
        return prefs.getString("fileHash", "");
    }

    // Save new hash to SharedPreferences
    private void saveFileHash(Context context, String newHash) {
        SharedPreferences prefs = context.getSharedPreferences("PlantCarePrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("fileHash", newHash).apply();
    }


    private void populateDatabase(Context context) {
        List<PlantCare> plantCareList = CsvParser.parseCsv(context, "plant_care.csv");
        if (!plantCareList.isEmpty()) {
            plantCareDao().insertAll(plantCareList);
            Log.d("PlantCareDatabase", "Database populated with CSV data.");

            // Fetch and log the inserted records
            List<PlantCare> insertedPlants = plantCareDao().getAllPlants();
            Log.d("PlantCareDatabase", "Inserted records count: " + insertedPlants.size());
            for (PlantCare plant : insertedPlants) {
                Log.d("PlantCareDatabase", "Plant: " + plant.getCommonName() +
                        ", Scientific Name: " + plant.getScientificName() +
                        ", Watering Frequency: " + plant.getWateringFrequency() +
                        ", Sunlight: " + plant.getSunlight() +
                        ", Soil Type: " + plant.getSoilType());
            }
        } else {
            Log.e("PlantCareDatabase", "CSV data is empty or failed to parse.");
        }
    }
}

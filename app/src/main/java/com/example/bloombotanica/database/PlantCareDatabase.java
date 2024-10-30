package com.example.bloombotanica.database;

import android.content.Context;
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

import java.util.List;

@Database(entities = {PlantCare.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class PlantCareDatabase extends RoomDatabase {

    private static volatile PlantCareDatabase instance;

    public abstract PlantCareDao plantCareDao();

    public static synchronized PlantCareDatabase getInstance(Context context) {
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
                    })
                    .build();
        }
        return instance;
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
                Log.d("PlantCareDatabase", "Plant: " + plant.getPlantName() +
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

package com.example.bloombotanica;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantsActivity extends AppCompatActivity {

    private RecyclerView plantRecyclerView;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plants);

        // Set the date
        TextView dateText = findViewById(R.id.date_text);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE\nMMMM dd", Locale.getDefault());
        dateText.setText(dateFormat.format(new Date()));

        // Initialize plant list from saved data
        plantList = loadPlants();
        plantAdapter = new PlantAdapter(plantList);

        // Set up RecyclerView
        plantRecyclerView = findViewById(R.id.plant_recycler_view);
        plantRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        plantRecyclerView.setAdapter(plantAdapter);

        // Add plant button
        findViewById(R.id.add_plant_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show dialog or navigate to another activity to add a new plant
                // Example of adding a default plant
                Plant newPlant = new Plant("New Plant", R.drawable.status_icon_orange);
                plantList.add(newPlant);
                plantAdapter.addPlant(newPlant);
                savePlants();
            }
        });
    }

    private void savePlants() {
        // Save plant list to shared preferences as JSON
        String json = new Gson().toJson(plantList);
        getSharedPreferences("plant_prefs", MODE_PRIVATE)
                .edit()
                .putString("plant_list", json)
                .apply();
    }

    private List<Plant> loadPlants() {
        // Load plant list from shared preferences
        String json = getSharedPreferences("plant_prefs", MODE_PRIVATE)
                .getString("plant_list", null);
        if (json != null) {
            Type type = new TypeToken<List<Plant>>() {}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }
}

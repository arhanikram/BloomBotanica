package com.example.bloombotanica.dialogs;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.PlantSuggestionAdapter;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.database.UserPlantDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Calendar;

public class AddPlantDialogFragment extends DialogFragment implements PlantSuggestionAdapter.SuggestionClickListener {

    private UserPlantDatabase userpdb;
    private PlantCareDatabase plantCaredb;
    private RecyclerView suggestionsRecyclerView;
    private PlantSuggestionAdapter suggestionsAdapter;
    private EditText plantNameSelector, plantNicknameInput;
    private Button addPlantButton;
    private ConstraintLayout addPlantDialog;

    public interface OnPlantAddedListener {
        void onPlantAdded(UserPlant newPlant);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_plant_dialog, container, false);
        addPlantDialog = view.findViewById(R.id.add_plant_dialog);

        Log.d("AddPlantDialogFragment", "onCreateView called");

        // Initialize the database
        userpdb = UserPlantDatabase.getInstance(requireContext());
        Log.d("AddPlantDialogFragment", "Database initialized: " + (userpdb != null));

        plantNicknameInput = view.findViewById(R.id.plant_nickname_input);
        addPlantButton = view.findViewById(R.id.add_plant_button);


        plantCaredb = PlantCareDatabase.getInstance(requireContext());
        Log.d("AddPlantDialogFragment", "PlantCareDatabase initialized: " + (plantCaredb != null));
        plantNameSelector = view.findViewById(R.id.plant_name_selector);
        suggestionsRecyclerView = view.findViewById(R.id.suggestions_recycler_view);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        suggestionsAdapter = new PlantSuggestionAdapter(this);
        suggestionsRecyclerView.setAdapter(suggestionsAdapter);

        plantNameSelector.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.length() > 0) {
                    searchPlantSuggestions(s.toString());
                } else {
                    suggestionsRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        addPlantButton.setOnClickListener(v -> {
            String plantNickname = plantNicknameInput.getText().toString();
            String plantName = plantNameSelector.getText().toString();

            // Retrieve the current list of suggestions from the adapter
            List<PlantCare> suggestions = suggestionsAdapter.getSuggestions();
            boolean isPlantNameValid = false;
            for (PlantCare plant : suggestions) {
                if (plant.getCommonOrScientificName(plantName).equalsIgnoreCase(plantName)) {
                    isPlantNameValid = true;
                    break;
                }
            }

            if (plantNickname.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a plant nickname", Toast.LENGTH_SHORT).show();
                return;
            }

            if (plantName.isEmpty()) {
                Toast.makeText(getContext(), "Please select a plant from the suggestions", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPlantNameValid) {
                Toast.makeText(getContext(), "Please select a valid plant name from the suggestions", Toast.LENGTH_SHORT).show();
                return;
            }

            // if both fields are valid - proceed to add the plant
            addPlantToDatabase(plantNickname, plantName);
        });

        return view;
    }

    private void searchPlantSuggestions(String query) {
        new Thread(() -> {
            List<PlantCare> matchingPlants = plantCaredb.plantCareDao().searchPlants(query);

            // Use a LinkedHashSet to filter out duplicate plant names based on the search result
            Set<String> uniqueNames = new LinkedHashSet<>();
            List<PlantCare> filteredPlants = new ArrayList<>();

            for (PlantCare plant : matchingPlants) {
                String name = plant.getCommonOrScientificName(query);
                if (uniqueNames.add(name)) {
                    filteredPlants.add(plant);
                }
            }
            requireActivity().runOnUiThread(() -> {
                // Update the suggestions in the adapter
                suggestionsAdapter.updateSuggestions(filteredPlants, query);
                // Adjust visibility based on results
                if (filteredPlants.isEmpty() || Objects.equals(filteredPlants.get(0).getCommonOrScientificName(query), query)) {
                    suggestionsRecyclerView.setVisibility(View.GONE);
                } else {
                    suggestionsRecyclerView.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams layoutParams = suggestionsRecyclerView.getLayoutParams();
                    layoutParams.height = filteredPlants.size() > 3
                            ? getResources().getDimensionPixelSize(R.dimen.suggestions_max_height)
                            : ViewGroup.LayoutParams.WRAP_CONTENT;
                    suggestionsRecyclerView.setLayoutParams(layoutParams);
                }
            });
        }).start();
    }

    public void onSuggestionClick(String plantName) {
        plantNameSelector.setText(plantName);
        suggestionsRecyclerView.setVisibility(View.GONE);
        Log.d("AddPlantDialogFragment", "onSuggestionClick called with plantName: " + plantName);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void addPlantToDatabase(String plantNickname, String plantName) {
        new Thread(() -> {
            Log.d("AddPlantDialogFragment", "addPlantToDatabase called");
            PlantCare plant = plantCaredb.plantCareDao().searchPlants(plantName).get(0);
            int wateringFrequency = plant.getWateringFrequency();

            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_YEAR, wateringFrequency);

            //checking next water date and date added in logs
            Log.d("AddPlantDialogFragment", "Next watering date: " + calendar.getTime());
            Log.d("AddPlantDialogFragment", "Date added: " + today);

            UserPlant newPlant = new UserPlant(plant.getId(), plantNickname, today, today);
            newPlant.setNextWateringDate(calendar.getTime());
            Log.d("AddPlantDialogFragment", "New plant created");

            Log.d("AddPlantDialogFragment", "Inside thread to add plant to database");
            userpdb.userPlantDao().insert(newPlant);
            Log.d("AddPlantDialogFragment", "Database insert completed");

            // Run on the main thread after adding to the database
            requireActivity().runOnUiThread(() -> {
                if (getTargetFragment() instanceof OnPlantAddedListener) {
                    ((OnPlantAddedListener) getTargetFragment()).onPlantAdded(newPlant);
                }
                dismiss(); // Close the dialog
            });
        }).start();
    }
}

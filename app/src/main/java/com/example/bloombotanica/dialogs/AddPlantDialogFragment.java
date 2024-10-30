package com.example.bloombotanica.dialogs;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.ui.PlantsFragment;

import java.util.Date;

public class AddPlantDialogFragment extends DialogFragment {

    private UserPlantDatabase userpdb;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_plant_dialog, container, false);

        Log.d("AddPlantDialogFragment", "onCreateView called");

        // Initialize the database
        userpdb = UserPlantDatabase.getInstance(requireContext());
        Log.d("AddPlantDialogFragment", "Database initialized: " + (userpdb != null));

        EditText plantNicknameInput = view.findViewById(R.id.plant_nickname_input);
        Button addPlantButton = view.findViewById(R.id.add_plant_button);

        addPlantButton.setOnClickListener(v -> {
            String plantNickname = plantNicknameInput.getText().toString();
            if (!plantNickname.isEmpty()) {
                addPlantToDatabase(plantNickname);
            } else {
                Toast.makeText(getContext(), "Please enter a plant nickname", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void addPlantToDatabase(String plantNickname) {
        UserPlant newPlant = new UserPlant(1, plantNickname, new Date(), new Date());

        Log.d("AddPlantDialogFragment", "addPlantToDatabase called");

        new Thread(() -> {
            Log.d("AddPlantDialogFragment", "Inside thread to add plant to database");
            userpdb.userPlantDao().insert(newPlant);
            Log.d("AddPlantDialogFragment", "Database insert completed");
            requireActivity().runOnUiThread(() -> {
                try {
                    PlantsFragment plantsFragment = (PlantsFragment) requireActivity()
                            .getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container); // Replace with your fragment container ID

                    if (plantsFragment != null) {
                        plantsFragment.addPlant(newPlant);
                        Log.d("AddPlantDialogFragment", "Attempting to update UI in PlantsFragment");
                    } else {
                        Log.e("AddPlantDialogFragment", "PlantsFragment is null, cannot update UI");
                    }
                } catch (Exception e) {
                    Log.e("AddPlantDialogFragment", "Error updating UI in PlantsFragment", e);
                }
                dismiss(); // Close the dialog
            });
        }).start();

    }

}
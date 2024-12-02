package com.example.bloombotanica.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.PlantSuggestionAdapter;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;

import java.util.Calendar;
import java.util.Date;

public class AddPlantNicknameDialog extends DialogFragment {

    private PlantCare plantCare;
    private EditText plantNicknameInput;
    private Button addPlantButton;
    private UserPlantDatabase userpdb;
    private String imagePath;

    public interface OnPlantAddedListener {
        void onPlantAdded(UserPlant newPlant);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_plant_nickname_dialog, container, false);

        // Retrieve the passed PlantCare object from the arguments
        if (getArguments() != null && getArguments().containsKey("plantCare")) {
            plantCare = (PlantCare) getArguments().getSerializable("plantCare");
            imagePath = getArguments().getString("imagePath");
            Log.d("AddPlantNicknameDialog", "PlantCare object received: " + plantCare.getCommonName());
            //log id
            Log.d("AddPlantNicknameDialog", "PlantCare object id received: " + plantCare.getId());
        } else {
            Log.d("AddPlantNicknameDialog", "PlantCare object not received");
        }

        // Initialize UI elements
        plantNicknameInput = view.findViewById(R.id.plant_nickname_input);
        addPlantButton = view.findViewById(R.id.add_plant_button);

        userpdb = UserPlantDatabase.getInstance(requireContext());

        addPlantButton.setOnClickListener(v -> {
            String plantNickname = plantNicknameInput.getText().toString();

            if (plantNickname.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a plant nickname", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed to add the plant to the database
            addPlantToDatabase(plantNickname, plantCare, imagePath);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void createTask(int userPlantId, Date nextDueDate, String taskType) {
        new Thread(() -> {
            Task newTask = new Task(userPlantId, taskType, nextDueDate, false);
            UserPlantDatabase.getInstance(getContext()).taskDao().insert(newTask);
            Log.d("AddPlantDialogFragment", "Task created: ID=" + userPlantId + ", Type=" + taskType + ", DueDate=" + nextDueDate);

        }).start();
    }

    private void addPlantToDatabase(String plantNickname, PlantCare plantCare, String imagePath) {
        new Thread(() -> {
            Log.d("AddPlantNicknameDialog", "Adding plant to database: " + plantNickname);
            if (plantCare != null) {
                Log.d("AddPlantNicknameDialog", "PlantCare object received: " + plantCare.getCommonName());
            } else {
                Log.d("AddPlantNicknameDialog", "PlantCare object not received");
            }
            // Add the plant using the plantCare data
            String plantName = plantCare.getCommonName();
            int wateringFrequency = plantCare.getWateringFrequency();
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_YEAR, wateringFrequency);

            UserPlant newPlant = new UserPlant(plantCare.getId(), plantNickname, today);
            newPlant.setNextWateringDate(calendar.getTime());
            newPlant.setImagePath(imagePath);

            userpdb.userPlantDao().insert(newPlant);
            Log.d("AddPlantNicknameDialog", "Plant added to database: " + newPlant.getNickname());

            UserPlant insertedPlant = userpdb.userPlantDao().getUserPlantByNickname(plantNickname);


            // Create tasks, journal entries, etc.
            createTask(insertedPlant.getId(), new Date(), "Water");
            createTask(insertedPlant.getId(), new Date(), "Rotate");

            //log the plant added date in journal
            JournalEntry entry = new JournalEntry();
            entry.setPlantId(insertedPlant.getId());
            entry.setTimestamp(today);
            entry.setTitle("Plant added");
            try {
                userpdb.journalEntryDao().insert(entry);
                Log.d("AddPlantNicknameDialog", "Journal entry created for plant: " + insertedPlant.getId());
            } catch (Exception e) {
                Log.e("AddPlantNicknameDialog", "Error inserting journal entry", e);
            }
            requireActivity().runOnUiThread(() -> {
                // Ensure the parent activity implements OnPlantAddedListener
                if (requireActivity() instanceof OnPlantAddedListener) {
                    OnPlantAddedListener listener = (OnPlantAddedListener) requireActivity();
                    listener.onPlantAdded(newPlant);  // Notify the listener (MainActivity)
                }
                dismiss();  // Close the dialog
            });

        }).start();
    }
}

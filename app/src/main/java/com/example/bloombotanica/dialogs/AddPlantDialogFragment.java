package com.example.bloombotanica.dialogs;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;


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
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.utils.TaskNotificationWorker;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AddPlantDialogFragment extends DialogFragment implements PlantSuggestionAdapter.SuggestionClickListener {

    private UserPlantDatabase userpdb;
    private PlantCareDatabase plantCaredb;
    private RecyclerView suggestionsRecyclerView;
    private PlantSuggestionAdapter suggestionsAdapter;
    private EditText plantNameSelector, plantNicknameInput;
    private Button addPlantButton;
    private ConstraintLayout addPlantDialog;
    private Task memTask;
    private TaskDao taskDao;

    private boolean TESTING = false; //SET TO TRUE FOR TESTING INSTANT NOTIFICATIONS

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
        taskDao = userpdb.taskDao();

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

    private void createTask(int userPlantId, Date nextDueDate, String taskType) {
        new Thread(() -> {
            Task newTask = new Task(userPlantId, taskType, nextDueDate, false);
            UserPlantDatabase.getInstance(getContext()).taskDao().insert(newTask);
            Log.d("AddPlantDialogFragment", "Task created: ID=" + userPlantId + ", Type=" + taskType + ", DueDate=" + nextDueDate);
            memTask = taskDao.getTaskForUserPlantAndType(userPlantId, taskType);
            requestNotificationPermissionAndSchedule(memTask);
        }).start();
    }

    private void addPlantToDatabase(String plantNickname, String plantName) {
        new Thread(() -> {
            Log.d("AddPlantDialogFragment", "addPlantToDatabase called");
            PlantCare plant = plantCaredb.plantCareDao().searchPlants(plantName).get(0);
            Log.d("AddPlantDialogFragment", "Plant found: " + plant.getCommonName());
            Log.d(TAG, String.valueOf(plant.getId()));
            Log.d(TAG, plantName + " " + plantCaredb.plantCareDao().searchPlants(plantName).get(0).getCommonName());
            int wateringFrequency = plant.getWateringFrequency();

            Date today = new Date();
            Calendar nextWater = Calendar.getInstance();
            nextWater.setTime(today);
            nextWater.add(Calendar.DAY_OF_YEAR, wateringFrequency);

            Calendar nextTurn = Calendar.getInstance();
            nextTurn.setTime(today);
            nextTurn.add(Calendar.DAY_OF_YEAR, plant.getTurningFrequency());

            //checking next water date and date added in logs
            Log.d("AddPlantDialogFragment", "Next watering date: " + nextWater.getTime());
            Log.d("AddPlantDialogFragment", "Next turning date: " + nextTurn.getTime());
            Log.d("AddPlantDialogFragment", "Date added: " + today);

            UserPlant newPlant = new UserPlant(plant.getId(), plantNickname, today);
            newPlant.setNextWateringDate(nextWater.getTime());
            newPlant.setNextTurningDate(nextTurn.getTime());
            Log.d("AddPlantDialogFragment", "New plant created");

            userpdb.userPlantDao().insert(newPlant);
            Log.d("AddPlantDialogFragment", "Database insert completed");

            UserPlant insertedPlant = userpdb.userPlantDao().getUserPlantByNickname(plantNickname);

            createTask(insertedPlant.getId(), new Date(), "Water");
            createTask(insertedPlant.getId(), new Date(), "Rotate");

            //log the plant added date in journal
            JournalEntry entry = new JournalEntry();
            entry.setPlantId(insertedPlant.getId());
            entry.setTimestamp(today);
            entry.setTitle("Plant added");
            userpdb.journalEntryDao().insert(entry);

            Log.d("AddPlantDialogFragment", "Task created");
            // Run on the main thread after adding to the database
            requireActivity().runOnUiThread(() -> {
                if (getTargetFragment() instanceof OnPlantAddedListener) {
                    ((OnPlantAddedListener) getTargetFragment()).onPlantAdded(newPlant);
                }
                dismiss(); // Close the dialog
            });
        }).start();
    }

    private void scheduleTaskNotification(Task task, boolean forTesting) {
        Log.d("AddPlantDialogFragment", "scheduleTaskNotification called");
        // Use WorkManager to schedule the notification worker
        Calendar calendar = Calendar.getInstance();
        if (forTesting) {
            calendar.setTime(new Date());
        } else {
            calendar.setTime(task.getDueDate());
        }
        calendar.set(Calendar.HOUR_OF_DAY, 12); // Set to 12:00 PM on the due date
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long delayInMillis = forTesting ? 0 : calendar.getTimeInMillis() - System.currentTimeMillis();

        // Create the input data for the worker
        Data inputData = new Data.Builder()
                .putInt("taskId", task.getId())
                .putString("taskType", task.getTaskType())
                .putLong("dueDateMillis", task.getDueDate().getTime())
                .build();

        // Create a OneTimeWorkRequest to trigger the notification
        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(TaskNotificationWorker.class)
                .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        // Enqueue the work request to WorkManager
        WorkManager.getInstance(requireContext()).enqueue(notificationWorkRequest);

                Log.d("TaskNotification", "Notification scheduled for task " + task.getId() + " at " + calendar.getTime());
    }

    //notif permissions
    // Example method that checks if notification permission is granted
    private void requestNotificationPermissionAndSchedule(Task task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // 33 is Tiramisu
            // Only request the permission if the SDK version is 33 or higher
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule the notification
                scheduleTaskNotification(task, TESTING);
            } else {
                // Request permission if not granted
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                if (isAdded() && getContext() != null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "We need permission to send notifications for task reminders.", Toast.LENGTH_LONG).show());
                }
            }
        } else {
            // API level below 33, notifications can be scheduled normally without this permission
            scheduleTaskNotification(task, TESTING);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            // Handle the permission result
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                scheduleTaskNotification(memTask, TESTING);
            } else {
                // Permission denied
                Toast.makeText(getContext(), "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

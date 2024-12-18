package com.example.bloombotanica.dialogs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.PlantSuggestionAdapter;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.utils.TaskNotificationWorker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddPlantNicknameDialog extends DialogFragment {

    private PlantCare plantCare;
    private EditText plantNicknameInput;
    private Button addPlantButton;
    private UserPlantDatabase userpdb;
    private String imagePath;
    private Task memTask;
    private TaskDao taskDao;

    private boolean TESTING = false; //SET TO TRUE FOR TESTING INSTANT NOTIFICATIONS

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
        taskDao = userpdb.taskDao();

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
            memTask = taskDao.getTaskForUserPlantAndType(userPlantId, taskType);
            requestNotificationPermissionAndSchedule(memTask);
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
                createTask(insertedPlant.getId(), new Date(), "Water");
                createTask(insertedPlant.getId(), new Date(), "Rotate");
//                dismiss();  // Close the dialog
            });

        }).start();
    }

    private void scheduleTaskNotification(Task task, boolean forTesting) {
        Log.d("AddPlantDialogFragment", "scheduleTaskNotification called");

        // Create a Calendar instance for scheduling the notification
        Calendar calendar = Calendar.getInstance();
        if (forTesting) {
            calendar.setTime(new Date());  // Set to current date for testing
        } else {
            calendar.setTime(task.getDueDate());  // Set to task's due date
        }

        // Set the time to 12:00 PM (for consistency)
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long delayInMillis = forTesting ? 5000 : calendar.getTimeInMillis() - System.currentTimeMillis();  // Delay until the due time

        // Prepare the input data for the worker
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

        Log.d("TaskNotificationPrediction", "Notification scheduled for task " + task.getId() + " at " + calendar.getTime());
    }

    private void requestNotificationPermissionAndSchedule(Task task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13 or higher
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule the notification
                scheduleTaskNotification(task, TESTING);
                dismiss();
            } else {
                // Request permission if not granted
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                if (isAdded() && getContext() != null) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "We need permission to send notifications for task reminders.", Toast.LENGTH_LONG).show();
                        dismiss();
                    });
                }
            }
        } else {
            // For API levels below 33, schedule without permission check
            scheduleTaskNotification(task, TESTING);
            dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule the notification
                scheduleTaskNotification(memTask, TESTING);
                dismiss();
            } else {
                // Permission denied, show a toast
                Toast.makeText(getContext(), "Notification permission denied.", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

}

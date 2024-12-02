package com.example.bloombotanica.utils;

import android.util.Log;

import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class TaskUtils {

    public static void renewTask(Task task, TaskDao taskDao, UserPlantDao userPlantDao, PlantCareDatabase plantCareDatabase, Runnable onComplete) {
        AtomicReference<String> journalTitle = new AtomicReference<>("");


        new Thread(() -> {
            // Mark the current task as completed
            Log.d("TaskUtils", "Marking task as completed: " + task.getId());
            taskDao.markTaskAsCompleted(task.getId());
            Log.d("TaskUtils", "Task is completed: " + task.isCompleted());

            // Get related user userPlant
            UserPlant userPlant = userPlantDao.getUserPlantById(task.getUserPlantId());
            if (userPlant != null) {
                // Get userPlant care details
                PlantCare plantCare = plantCareDatabase.plantCareDao().getPlantCareById(userPlant.getPlantCareId());
                if (plantCare != null) {
                    // Check task type and renew accordingly
                    Date today = new Date();
                    Calendar nextDueDate = Calendar.getInstance();
                    boolean taskUpdated = false;

                    if ("Water".equals(task.getTaskType())) {
                        // Renew the watering task
                        int wateringFrequency = plantCare.getWateringFrequency();
                        nextDueDate.setTime(today);
                        nextDueDate.add(Calendar.DAY_OF_YEAR, wateringFrequency); // Add watering frequency days
                        userPlant.setNextWateringDate(nextDueDate.getTime()); // Update the userPlant's next watering date
                        userPlant.setLastWatered(today);
                        userPlantDao.updateWateringDates(userPlant.getId(), today, userPlant.getNextWateringDate());
                        journalTitle.set("Watered");
                        taskUpdated = true;
                    } else if ("Rotate".equals(task.getTaskType())) {
                        // Renew the turning task
                        int turningFrequency = plantCare.getTurningFrequency();
                        nextDueDate.setTime(today);
                        nextDueDate.add(Calendar.DAY_OF_YEAR, turningFrequency); // Add turning frequency days
                        userPlant.setNextTurningDate(nextDueDate.getTime()); // Update the userPlant's next turning date
                        userPlant.setLastTurned(today);
                        userPlantDao.updateTurningDates(userPlant.getId(), today, userPlant.getNextTurningDate());
                        journalTitle.set("Rotated");
                        taskUpdated = true;
                    }

                    if(taskUpdated) {
                        // Create a new task with the updated due date
                        Task newTask = new Task(
                                task.getUserPlantId(),
                                task.getTaskType(),
                                nextDueDate.getTime(),
                                false
                        );
                        // Insert the new task into the database
                        taskDao.insert(newTask);

                        UserPlantDatabase userPlantDatabase = UserPlantDatabase.getInstance(null);

                        JournalEntry entry = new JournalEntry();
                        entry.setPlantId(userPlant.getId());
                        entry.setTimestamp(today);
                        entry.setTitle(journalTitle.get());
                        userPlantDatabase.journalEntryDao().insert(entry);

                    } else {
                        Log.e("TaskUtils", "Invalid task type: " + task.getTaskType());
                    }
                } else {
                    Log.e("TaskUtils", "PlantCare details not found for userPlant: " + userPlant.getNickname());
                }
            } else {
                Log.e("TaskUtils", "UserPlant not found for task: " + task.getId());
            }

            // Invoke the completion callback (e.g., to refresh UI)
            if (onComplete != null) {
                onComplete.run();
            }
        }).start();
    }
}

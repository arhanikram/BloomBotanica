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

public class TaskUtils {
    public static void renewTask(Task task, TaskDao taskDao, UserPlantDao userPlantDao, PlantCareDatabase plantCareDatabase, Runnable onComplete) {
        new Thread(() -> {
            // Mark the current task as completed
            taskDao.markTaskAsCompleted(task.getId());

            // Get related user plant
            UserPlant plant = userPlantDao.getUserPlantById(task.getUserPlantId());
            if (plant != null) {
                // Get plant care details
                PlantCare plantCare = plantCareDatabase.plantCareDao().getPlantCareById(plant.getPlantCareId());
                if (plantCare != null) {
                    // Get watering frequency
                    int wateringFrequency = plantCare.getWateringFrequency();

                    // Calculate next due date based on current task's due date
                    Date nextDueDate = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(nextDueDate);
                    calendar.add(Calendar.DAY_OF_YEAR, wateringFrequency);

                    Date today = new Date();
                    plant.setLastWatered(today);
                    plant.setNextWateringDate(calendar.getTime());
                    plant.setWatered(true);


                    // Create a new task with the updated due date
                    Task newTask = new Task(
                            task.getUserPlantId(),
                            task.getTaskType(),
                            calendar.getTime(),
                            false
                    );

                    // Insert the new task into the database
                    taskDao.insert(newTask);

                    plant.setLastWatered(today);

                    UserPlantDatabase userPlantDatabase = UserPlantDatabase.getInstance(null);

                    JournalEntry entry = new JournalEntry();
                    entry.setPlantId(plant.getId());
                    entry.setTimestamp(today);
                    entry.setTitle("Watered");
                    userPlantDatabase.journalEntryDao().insert(entry);

                    userPlantDao.updateWateringDates(plant.getId(), today, plant.getNextWateringDate());
                    Log.d("TaskUtils", "New task created for plant: " + plant.getNickname() + " - " + newTask.toString());
                } else {
                    Log.e("TaskUtils", "PlantCare details not found for plant: " + plant.getNickname());
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

package com.example.bloombotanica.utils;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.bloombotanica.R;

import java.util.Date;

public class TaskNotificationWorker extends Worker {

    public static final String CHANNEL_ID = "task_reminders";  // Use a constant for the channel ID

    public TaskNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d("TaskNotificationWorker", "TaskNotificationWorker created");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("TaskNotificationWorker", "doWork called");

        // Ensure the notification channel is created (only once per app lifecycle)
        createNotificationChannel();

        // Retrieve input data
        int taskId = getInputData().getInt("taskId", -1);
        String taskType = getInputData().getString("taskType");
        long dueDateMillis = getInputData().getLong("dueDateMillis", -1);

        // Validate input data and show the notification
        if (taskId != -1 && taskType != null && dueDateMillis != -1) {
            Log.d("TaskNotificationWorker", "Sending notification for task " + taskId);
            sendTaskNotification(taskId, taskType, new Date(dueDateMillis));
        } else {
            Log.e("TaskNotificationWorker", "Invalid input data");
            Log.d("TaskNotificationWorker", "taskId: " + taskId);
            Log.d("TaskNotificationWorker", "taskType: " + taskType);
            Log.d("TaskNotificationWorker", "dueDateMillis: " + dueDateMillis);
        }

        return Result.success();
    }

    private void createNotificationChannel() {
        // Only create the channel if it doesn't already exist
        CharSequence channelName = "Task Reminders";
        String channelDescription = "Notifications for task reminders.";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription(channelDescription);

        // Optionally, set other channel properties like vibration, sound, etc.
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 250, 250, 250});

        // Register the channel with the system
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendTaskNotification(int taskId, String taskType, Date dueDate) {
        Log.d("TaskNotificationWorker", "sendTaskNotification called");

        // Fallback title if taskType is null
        String contentText = (taskType != null) ? "It's time to " + taskType.toLowerCase() + " your plant!" : "It's time to care for your plant!";
        String title = "Plant Care Reminder";

        // Create a notification using NotificationManager
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.bloom_botanica) // Use your app's icon
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        // Show the notification
        notificationManager.notify(taskId, notification);
        Log.d("TaskNotificationWorker", "Notification sent for task " + taskId);
    }
}

package com.example.bloombotanica.ui;

import static android.content.Context.MODE_PRIVATE;
import static com.example.bloombotanica.utils.DateUtils.getStartAndEndOfDay;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.PlantGalleryAdapter;
import com.example.bloombotanica.adapters.TaskAdapter;
import com.example.bloombotanica.database.PlantCareDao;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.utils.TaskNotificationWorker;
import com.example.bloombotanica.utils.TaskUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.example.bloombotanica.utils.WeatherUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DashboardFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private TextView welcomeMessage, taskCount, weatherDate, temperature, humidity, sunlight, noTasks;
    private String welcomeText, userName;
    private UserPlantDatabase userpdb;
    private FusedLocationProviderClient fusedLocationClient;
    private ViewPager2 viewPager;  // Use ViewPager2 instead of ImageView
    private List<String> plantImages; // List to hold image names (e.g., "a1", "a2", ...)
    private Handler handler = new Handler();  // Handler for auto-scrolling
    private Runnable autoScrollRunnable; // Runnable to handle the scrolling task
    private List<UserPlant> userPlants = new ArrayList<>();  // Initialize as an empty list
    private PlantCareDatabase plantCareDatabase;
    private PlantCareDao plantCareDao;
    private SharedPreferences sharedPreferences;
    private Task memTask;

    private boolean TESTING = false; //SET TO TRUE FOR TESTING INSTANT NOTIFICATIONS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        plantCareDatabase = PlantCareDatabase.getInstance(requireContext());
        plantCareDao = plantCareDatabase.plantCareDao();

        // Get username from SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userName = sharedPreferences.getString("username", "");

        // Initialize UI components
        welcomeMessage = view.findViewById(R.id.welcomeMessage);
        taskCount = view.findViewById(R.id.taskCount);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        weatherDate = view.findViewById(R.id.weather_date);
        temperature = view.findViewById(R.id.temperature);
        humidity = view.findViewById(R.id.humidity);
        sunlight = view.findViewById(R.id.sunlight);
        viewPager = view.findViewById(R.id.plantGalleryPager);
        noTasks = view.findViewById(R.id.noTasks);

        // Initialize database and location services
        userpdb = UserPlantDatabase.getInstance(requireActivity().getApplicationContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Set the welcome message
        welcomeText = getString(R.string.welcome_username);
        welcomeMessage.setText(welcomeText);

        // Initialize the RecyclerView
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksRecyclerView.setLayoutFrozen(true);
        taskAdapter = new TaskAdapter(new ArrayList<>(), this::markTaskAsCompleted, userpdb.userPlantDao());
        tasksRecyclerView.setAdapter(taskAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("DashboardFragment", "OnResumeCalled");
        fetchUserLocation(); // Fetch weather data based on location
        loadUserPlants();   // Load user plants for image scrolling
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userName = sharedPreferences.getString("username", "");
        welcomeText = getString(R.string.welcome_username);
        welcomeMessage.setText(welcomeText + " " + userName);
        //TESTING
//        createOverdueTask(0, "Watering", 3); // Creates an overdue task due 3 days ago

        loadTasks(); // Load tasks
    }

    private void loadUserPlants() {
        new Thread(() -> {
            UserPlantDao userPlantDao = userpdb.userPlantDao();
            userPlants = userPlantDao.getAllUserPlants();
            List<Integer> plantImageIds = new ArrayList<>();  // Initialize a list for image IDs

            // Check if there are no plants
            if (userPlants.isEmpty()) {
                // If no plants, use the fallback image
                int fallbackImageResId = getResources().getIdentifier("bloom_botanica", "drawable", getContext().getPackageName());
                if (fallbackImageResId != 0) {
                    plantImageIds.add(fallbackImageResId);  // Add the fallback image
                } else {
                    // If the fallback image doesn't exist, add a default image
                    plantImageIds.add(R.drawable.default_plant_image);  // Replace with your default image resource
                }
            } else {
                // If there are plants, fetch their image resource IDs
                for (UserPlant userPlant : userPlants) {
                    String imageName = "a" + userPlant.getPlantCareId();  // Dynamically construct the image name
                    if (isAdded() && getContext() != null) {
                        int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                        Log.d("Image Debug", "Stored Image Path: " + userPlant.getImagePath());

                        Log.d("DashboardFragment", "Plant ID: " + userPlant.getId() + ", Image Name: " + imageName + ", Image Res ID: " + imageResId);

                        if (imageResId != 0) {
                            plantImageIds.add(imageResId);  // Add valid image resource ID
                        } else {
                            Log.d("DashboardFragment", "Image not found for plant ID: " + userPlant.getId());
                            plantImageIds.add(R.drawable.bloom_botanica);  // Use a default image if resource not found
                        }
                    }
                }
            }

            // Update the adapter on the main thread
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    PlantGalleryAdapter plantGalleryAdapter = new PlantGalleryAdapter(getContext(), plantImageIds);
                    viewPager.setAdapter(plantGalleryAdapter);
                    startAutoScrolling(plantImageIds);  // Pass the updated list to the auto-scrolling method
                });
            }
        }).start();
    }


    private void startAutoScrolling(List<Integer> plantImageIds) {
        if (plantImageIds == null || plantImageIds.isEmpty()) return;

        // Set up the auto-scrolling logic
        handler.postDelayed(autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % plantImageIds.size();
                viewPager.setCurrentItem(nextItem, true); // Smooth scrolling
                handler.postDelayed(this, 9000); // Repeat every 2 seconds
            }
        }, 5000); // Start after 2 seconds
    }

    private void fetchUserLocation() {
        Log.d("DashboardFragment", "fetchUserLocation Called");
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fetchWeatherData(location.getLatitude(), location.getLongitude());
                    }
                })
                .addOnFailureListener(e -> Log.e("DashboardFragment", "Location fetch failed", e));
    }

    private void fetchWeatherData(double latitude, double longitude) {
        String apiKey = "d9a614beccdb31bfe3b3d13bf1e6bd52";
        String weatherUrl = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=%s",
                latitude, longitude, apiKey);

        Log.d("DashboardFragment", "Fetching weather data from: " + weatherUrl);
        new Thread(() -> {
            try {
                URL url = new URL(weatherUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                if (isAdded()) {
                    parseWeatherData(response.toString());
                    Log.d("DashboardFragment", "Weather data fetched successfully");
                } else {
                    Log.e("DashboardFragment", "Fragment is not added to the activity");
                }
            } catch (Exception e) {
                Log.e("DashboardFragment", "Error fetching weather data", e);
            }
        }).start();
    }

    private void parseWeatherData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject main = jsonObject.getJSONObject("main");
            String temp = main.getString("temp");
            String hum = main.getString("humidity");
            String weatherCondition = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

            // Extract sunrise and sunset times from the response (in UNIX timestamp format)
            long sunriseUnix = jsonObject.getJSONObject("sys").getLong("sunrise");
            long sunsetUnix = jsonObject.getJSONObject("sys").getLong("sunset");

            // Calculate the light percentage using the WeatherUtils class
            double lightPercentage = WeatherUtils.calculateLightPercentage(sunriseUnix, sunsetUnix);

            // Format the light percentage as a string for display
            String sunlightValue = String.format(Locale.getDefault(), "%.0f%%", lightPercentage);

            // Update the UI with weather data
            requireActivity().runOnUiThread(() -> updateWeatherUI(temp, hum, sunlightValue, weatherCondition));
        } catch (JSONException e) {
            Log.e("DashboardFragment", "Error parsing weather data", e);
        }
    }


    private void updateWeatherUI(String temp, String hum, String sunlightValue, String condition) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        double tempVal = Double.parseDouble(temp);

        weatherDate.setText(currentDate);
        temperature.setText(String.format(Locale.US, "%.0f°C", tempVal));
        humidity.setText(String.format("%s%%", hum));
        sunlight.setText(sunlightValue);  // Display the calculated sunlight percentage
    }

    private void loadTasks() {
        Log.d("DashboardFragment", "loadTasks Called");
        new Thread(() -> {
            TaskDao taskDao = userpdb.taskDao();
            UserPlantDao userPlantDao = userpdb.userPlantDao();
            PlantCare plantCare = plantCareDao.getPlantCareById(0);
            Pair<Date, Date> dayRange = getStartAndEndOfDay(new Date());

            //commented out for testing purposes
            List<Task> todayTasks = taskDao.getIncompleteTasksForDate(dayRange.first, dayRange.second);
//            List<Task> todayTasks = taskDao.getIncompleteTasks();
            List<Task> overdueTasks = taskDao.getOverdueTasks(dayRange.first);

            Log.d("DashboardFragment", "Today tasks: " + todayTasks);
            Log.d("DashboardFragment", "Overdue tasks: " + overdueTasks.size());

            List<Task> combinedTasks = new ArrayList<>();
            if (!overdueTasks.isEmpty()) {
                combinedTasks.addAll(overdueTasks);
                Log.d("CombinedTasks", "Added overdue task to combinedTasks: " + overdueTasks);
            }
            if (!todayTasks.isEmpty()) {
                combinedTasks.addAll(todayTasks);
                Log.d("CombinedTasks", "Added today task to combinedTasks: " + todayTasks);
            }

            taskDao.removeTasksForDeletedPlants();

            taskDao.removeCompletedTasks();

            combinedTasks.sort((task1, task2) -> {
                if (task1.isOverdue() && !task2.isOverdue()) {
                    return -1;
                } else if (!task1.isOverdue() && task2.isOverdue()) {
                    return 1;
                } else {
                    return task1.getDueDate().compareTo(task2.getDueDate());
                }
            });

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    taskAdapter.updateTasks(combinedTasks);
                    tasksRecyclerView.setAdapter(taskAdapter);
                    if (combinedTasks.isEmpty()) {
                        tasksRecyclerView.setVisibility(View.GONE);
                        taskCount.setVisibility(View.GONE);
                        noTasks.setVisibility(View.VISIBLE);
                    } else {
                        tasksRecyclerView.setVisibility(View.VISIBLE);
                        taskCount.setVisibility(View.VISIBLE);
                        taskCount.setText("You have " + combinedTasks.size() + " tasks due!");
                        noTasks.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private void markTaskAsCompleted(Task task) {
        TaskDao taskDao = userpdb.taskDao();
        UserPlantDao userPlantDao = userpdb.userPlantDao();
        PlantCareDatabase plantCareDatabase = PlantCareDatabase.getInstance(getContext());
        // Reload tasks after task completion
        TaskUtils.renewTask(task, taskDao, userPlantDao, plantCareDatabase, this::loadTasks);
        memTask = task;
        requestNotificationPermissionAndSchedule(memTask);
    }

    // Method to create a task with a past due date
    private void createOverdueTask(int userPlantId, String taskType, int daysPastDue) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysPastDue); // Subtract days to set a past due date
        Date pastDueDate = calendar.getTime();


        Task overdueTask = new Task(
                userPlantId,
                taskType,
                pastDueDate,
                false // Not completed
        );

        new Thread(() -> {
            TaskDao taskDao = userpdb.taskDao();
            taskDao.insert(overdueTask);
            Log.d("DashboardFragment", "Inserted overdue task: " + overdueTask.toString());
            Log.d("TaskDao", "Current date for overdue query: " + new Date());
            requireActivity().runOnUiThread(this::loadTasks); // Refresh task list
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

        long delayInMillis = forTesting ? 0 : calendar.getTimeInMillis() - System.currentTimeMillis();  // Delay until the due time

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

        Log.d("TaskNotification", "Notification scheduled for task " + task.getId() + " at " + calendar.getTime());
    }

    private void requestNotificationPermissionAndSchedule(Task task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13 or higher
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
            // For API levels below 33, schedule without permission check
            scheduleTaskNotification(task, TESTING);
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
            } else {
                // Permission denied, show a toast
                Toast.makeText(getContext(), "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
package com.example.bloombotanica.ui;

import static android.content.Context.MODE_PRIVATE;
import static com.example.bloombotanica.utils.DateUtils.getStartAndEndOfDay;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

public class DashboardFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private TextView welcomeMessage, taskCount, weatherDate, temperature, humidity, sunlight, noTasks;
    private String welcomeText,userName;
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
        welcomeMessage.setText(welcomeText +" "+ userName);
        //TESTING
//        createOverdueTask(0, "Watering", 3); // Creates an overdue task due 3 days ago

        loadTasks(); // Load tasks
    }

    private void loadUserPlants() {
        new Thread(() -> {
            UserPlantDao userPlantDao = userpdb.userPlantDao();
            userPlants = userPlantDao.getAllUserPlants();
            List<Integer> plantImageIds = new ArrayList<>();  // Make sure we use this list

            // Fetch image resource IDs from plant IDs
            for (UserPlant userPlant : userPlants) {
                String imageName = "a" + userPlant.getPlantCareId();  // Construct the image name dynamically
                int imageResId = getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());
                Log.d("Image Debug", "Stored Image Path: " + userPlant.getImagePath());

                Log.d("DashboardFragment", "Plant ID: " + userPlant.getId() + ", Image Name: " + imageName + ", Image Res ID: " + imageResId);

                if (imageResId != 0) {
                    plantImageIds.add(imageResId);  // Add valid image resource ID
                } else {
                    Log.d("DashboardFragment", "Image not found for plant ID: " + userPlant.getId());
                    plantImageIds.add(R.drawable.default_plant_image);  // Use a default image if resource not found
                }
            }

            // Update the adapter on the main thread
            requireActivity().runOnUiThread(() -> {
                PlantGalleryAdapter plantGalleryAdapter = new PlantGalleryAdapter(getContext(), plantImageIds);
                viewPager.setAdapter(plantGalleryAdapter);
                startAutoScrolling(plantImageIds);  // Pass the correct list here
            });
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
            List<Task> todayTasks = taskDao.getTasksForDate(dayRange.first, dayRange.second);
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

}
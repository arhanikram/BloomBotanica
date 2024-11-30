package com.example.bloombotanica.ui;

import static android.content.Context.MODE_PRIVATE;

import static com.example.bloombotanica.utils.DateUtils.getStartAndEndOfDay;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.TaskAdapter;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.utils.TaskUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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
    private TextView welcomeMessage, taskCount, weatherDate, temperature, humidity, sunlight;
    private String welcomeText;
    private UserPlantDatabase userpdb;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "");

        assert container != null;
        welcomeMessage = view.findViewById(R.id.welcomeMessage);
        taskCount = view.findViewById(R.id.taskCount);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        weatherDate = view.findViewById(R.id.weather_date);
        temperature = view.findViewById(R.id.temperature);
        humidity = view.findViewById(R.id.humidity);
        sunlight = view.findViewById(R.id.sunlight);

        userpdb = UserPlantDatabase.getInstance(requireActivity().getApplicationContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        welcomeText = getString(R.string.welcome_username, userName);
        welcomeMessage.setText(welcomeText);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksRecyclerView.setAdapter(taskAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
        fetchUserLocation(); // Fetch weather data on resume
        new Thread(() -> {
            List<UserPlant> allPlants = userpdb.userPlantDao().getAllUserPlants();
            for (UserPlant plant : allPlants) {
                Log.d("ALLPLANTS", "Plant ID: " + plant.getId() + ", Nickname: " + plant.getNickname());
            }
        }).start();
    }

    private void fetchUserLocation() {
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
        String apiKey = "f3822f4c158ca8a944e2193b7191a838";
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

                //handling if user quickly leaves dashboard after trying to fetch weather data
                if(isAdded()) {
                    parseWeatherData(response.toString());
                    Log.d("DashboardFragment", "Weather data fetched successfully");
                } else {
                    Log.e("DashboardFragment", "Fragment is not added to the activity");
                }
            } catch (Exception e) {
                Log.e("DashboardFragment", "Error fetching weather data", e);
//                requireActivity().runOnUiThread(() ->
//                        Toast.makeText(getContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show());
                //this was causing an error when you open the app and instantly open another fragment before it processes weather data,
                //it would crash due to the ui update with requireactivity so i just commented it out
                //if it crashes it will show in logcat we dont really need a toast
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

            // Sunlight percentage placeholder (can use additional logic if available)
            String sunlightValue = "72%";

            requireActivity().runOnUiThread(() -> updateWeatherUI(temp, hum, sunlightValue, weatherCondition));
        } catch (JSONException e) {
            Log.e("DashboardFragment", "Error parsing weather data", e);
        }
    }

    private void updateWeatherUI(String temp, String hum, String sunlightValue, String condition) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        weatherDate.setText(currentDate);
        temperature.setText(String.format("%s°C", temp));
        humidity.setText(String.format("%s%%", hum));
        sunlight.setText(sunlightValue);
    }

    private void loadTasks() {
        new Thread(() -> {
            TaskDao taskDao = UserPlantDatabase.getInstance(getContext()).taskDao();
            UserPlantDao userPlantDao = UserPlantDatabase.getInstance(getContext()).userPlantDao();
            Pair<Date, Date> dayRange = getStartAndEndOfDay(new Date());

            //for testing purposes, show all pending tasks instead of only tasks for today
//             List<Task> todayTasks = taskDao.getIncompleteTasks(); //TESTING
             //to show only tasks for today, uncomment the following line and comment the line above
            List<Task> todayTasks = taskDao.getTasksForDate(dayRange.first, dayRange.second); //FINAL implementation
            List<Task> overdueTasks = taskDao.getOverdueTasks(new Date());

            // Combine today's tasks and overdue tasks
            List<Task> combinedTasks = new ArrayList<>();
            if (overdueTasks != null) combinedTasks.addAll(overdueTasks);
            if (todayTasks != null) combinedTasks.addAll(todayTasks);

            taskDao.removeTasksForDeletedPlants();

            //log all tasks
            for (Task task : combinedTasks) {
                Log.d("DashboardFragment", "All tasks: " + task.toString());
            }

            if (todayTasks == null) {
                todayTasks = new ArrayList<>();
            }

            List<Task> finalTodayTasks = todayTasks;

            // Sort tasks: overdue first, then by due date
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
                    taskAdapter = new TaskAdapter(combinedTasks, this::markTaskAsCompleted, userPlantDao);
                    tasksRecyclerView.setAdapter(taskAdapter);

                    if (combinedTasks.isEmpty()) {
                        tasksRecyclerView.setVisibility(View.GONE);
                        taskCount.setText(getString(R.string.task_count, 0));
                    } else {
                        tasksRecyclerView.setVisibility(View.VISIBLE);
                        taskCount.setText(getString(R.string.task_count, combinedTasks.size()));
                    }
                });
            }
        }).start();
    }

    private void markTaskAsCompleted(Task task) {
        TaskDao taskDao = UserPlantDatabase.getInstance(getContext()).taskDao();
        UserPlantDao userPlantDao = UserPlantDatabase.getInstance(getContext()).userPlantDao();
        PlantCareDatabase plantCareDatabase = PlantCareDatabase.getInstance(getContext());

        TaskUtils.renewTask(task, taskDao, userPlantDao, plantCareDatabase, () -> {
            loadTasks();
        });
    }
}

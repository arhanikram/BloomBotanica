package com.example.bloombotanica.ui;

import static android.content.Context.MODE_PRIVATE;

import static com.example.bloombotanica.utils.DateUtils.getStartAndEndOfDay;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private TextView welcomeMessage, taskCount;
    private String welcomeText;
    private UserPlantDatabase userpdb;

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
        userpdb = UserPlantDatabase.getInstance(requireActivity().getApplicationContext());

        welcomeText = getString(R.string.welcome_username, userName);
        welcomeMessage.setText(welcomeText);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksRecyclerView.setAdapter(taskAdapter);

//        loadTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
        new Thread(() -> {
            List<UserPlant> allPlants = userpdb.userPlantDao().getAllUserPlants();
            for (UserPlant plant : allPlants) {
                Log.d("ALLPLANTS", "Plant ID: " + plant.getId() + ", Nickname: " + plant.getNickname());
            }
        }).start();
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

            // Remove tasks for deleted plants
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

        TaskUtils.renewTask(task, taskDao, userPlantDao, plantCareDatabase, this::loadTasks);
    }
}
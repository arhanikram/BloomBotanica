package com.example.bloombotanica.ui;

import static com.example.bloombotanica.utils.DateUtils.getStartAndEndOfDay;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.TaskListAdapter;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.database.TaskDao;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.databinding.FragmentCalendarBinding;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.utils.RecurrenceUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private TaskDao taskDao;
    private TaskListAdapter taskListAdapter;
    private List<Task> tasksForSelectedDay = new ArrayList<>();
    private Set<CalendarDay> taskDates = new HashSet<>();
    private UserPlantDao userPlantDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentCalendarBinding.inflate(inflater, container, false);
        }
        taskDao = UserPlantDatabase.getInstance(requireContext()).taskDao();
        userPlantDao = UserPlantDatabase.getInstance(requireContext()).userPlantDao();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(binding != null) {

            MaterialCalendarView calendarView = binding.calendarView;
            RecyclerView taskRecyclerView = binding.taskRecyclerView;

            // Set up RecyclerView with TaskListAdapter
            taskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            taskListAdapter = new TaskListAdapter(tasksForSelectedDay, userPlantDao);
            taskRecyclerView.setAdapter(taskListAdapter);

            // Fetch task dates and decorate calendar
            fetchTaskDates();
            calendarView.addDecorator(new TaskDayDecorator(requireContext(), taskDates));

            calendarView.setDateSelected(CalendarDay.today(), true);
            fetchTasksForDate(new Date());

            // Listen for date selection
            calendarView.setOnDateChangedListener((widget, date, selected) -> {
                widget.invalidateDecorators();
                fetchTasksForDate(date.getDate());
            });
        }
    }

    private void fetchTaskDates() {
        new Thread(() -> {
            List<Task> allTasks = taskDao.getIncompleteTasks();
            for (Task task : allTasks) {
                // Get the plant's watering frequency
                UserPlant plant = userPlantDao.getUserPlantById(task.getUserPlantId());
                if (plant != null) {
                    PlantCare plantCare = PlantCareDatabase.getInstance(requireContext())
                            .plantCareDao()
                            .getPlantCareById(plant.getPlantCareId());

                    if (plantCare != null) {
                        int frequencyInDays = plantCare.getWateringFrequency();
                        List<Date> futureDates = RecurrenceUtils.calculateFutureDates(
                                task.getDueDate(), // Start with the next due date
                                frequencyInDays,
                                6 // Show due dates up to 6 months ahead
                        );

                        for (Date futureDate : futureDates) {
                            CalendarDay calendarDay = CalendarDay.from(futureDate);
                            taskDates.add(calendarDay);
                        }
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                if(binding != null) {
                    binding.calendarView.invalidateDecorators(); // Refresh decorators
                }
            });
        }).start();
    }

    private void fetchTasksForDate(Date selectedDate) {
        new Thread(() -> {
            Pair<Date, Date> dayRange = getStartAndEndOfDay(selectedDate);
            Set<Task> uniqueTasks = new HashSet<>(taskDao.getTasksForDate(dayRange.first, dayRange.second)); // Use a HashSet for uniqueness

            // Add recurring tasks
            List<Task> allTasks = taskDao.getIncompleteTasks();
            for (Task task : allTasks) {
                UserPlant plant = userPlantDao.getUserPlantById(task.getUserPlantId());
                if (plant != null) {
                    PlantCare plantCare = PlantCareDatabase.getInstance(requireContext())
                            .plantCareDao()
                            .getPlantCareById(plant.getPlantCareId());

                    if (plantCare != null) {
                        int frequencyInDays = plantCare.getWateringFrequency();
                        List<Date> futureDates = RecurrenceUtils.calculateFutureDates(
                                task.getDueDate(),
                                frequencyInDays,
                                6 // Look 6 months ahead
                        );

                        // Check if selected date matches any recurrence
                        for (Date futureDate : futureDates) {
                            if (!futureDate.before(dayRange.first) && !futureDate.after(dayRange.second)) {
                                uniqueTasks.add(task); // Add to Set to ensure uniqueness
                                break;
                            }
                        }
                    }
                }
            }

            // Convert Set back to List for RecyclerView
            List<Task> tasksForDate = new ArrayList<>(uniqueTasks);

            // Update RecyclerView
            requireActivity().runOnUiThread(() -> {
                taskListAdapter.updateTasks(tasksForDate);

                if (tasksForDate.isEmpty()) {
                    binding.emptyTaskMessage.setVisibility(View.VISIBLE);
                    binding.taskRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyTaskMessage.setVisibility(View.GONE);
                    binding.taskRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    public static class TaskDayDecorator implements DayViewDecorator {

        private final Context context;
        private final Set<CalendarDay> datesWithTasks;

        public TaskDayDecorator(Context context, Set<CalendarDay> datesWithTasks) {
            this.context = context;
            this.datesWithTasks = datesWithTasks;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return datesWithTasks.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(
                    Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.status_icon_orange))
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

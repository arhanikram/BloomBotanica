package com.example.bloombotanica.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.database.UserPlantDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private final UserPlantDao userPlantDao;
    private Map<String, Pair<Integer, Integer>> taskIconMap;

    public TaskListAdapter(List<Task> tasks, UserPlantDao userPlantDao) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.userPlantDao = userPlantDao;

        taskIconMap = new HashMap<>();
        taskIconMap.put("Water", new Pair<>(R.drawable.water_drop_svgrepo_com, R.color.teal_700));
        taskIconMap.put("Rotate", new Pair<>(R.drawable.rotate_svgrepo_com, R.color.lightergreen));
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskName.setText(task.getTaskType());

        String taskType = task.getTaskType();

        // Get the icon and tint for the task type
        Pair<Integer, Integer> iconAndTint = getTaskIconAndTint(taskType);

        // Set the icon dynamically using the map
        holder.taskIcon.setImageResource(iconAndTint.first);

        // Set the tint dynamically using the map
        if (iconAndTint.second != 0) { // Ensure the tint is not 0 (default)
            holder.taskIcon.setImageTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), iconAndTint.second));
        } else {
            // Apply a default tint if needed (for example, no tint or use a neutral color)
            holder.taskIcon.setImageTintList(null);
        }

        // Fetch plant name from UserPlantDao
        new Thread(() -> {
            String plantName = userPlantDao.getUserPlantById(task.getUserPlantId()) != null
                    ? userPlantDao.getUserPlantById(task.getUserPlantId()).getNickname()
                    : "Unknown Plant";
            holder.itemView.post(() -> holder.plantName.setText(plantName));
        }).start();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        tasks = newTasks != null ? newTasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Method to get the icon resource and tint based on task type
    public Pair<Integer, Integer> getTaskIconAndTint(String taskType) {
        return taskIconMap.get(taskType); // Default if not found
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, plantName;
        ImageView taskIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            plantName = itemView.findViewById(R.id.task_plant_name);
            taskIcon = itemView.findViewById(R.id.task_icon);
        }
    }
}

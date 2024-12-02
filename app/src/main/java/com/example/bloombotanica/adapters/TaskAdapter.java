package com.example.bloombotanica.adapters;

import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bloombotanica.R;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskCompletedListener listener;
    private UserPlantDao userPlantDao;
    private Map<String, Pair<Integer, Integer>> taskIconMap;

    public TaskAdapter(List<Task> tasks, OnTaskCompletedListener listener, UserPlantDao userPlantDao) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.listener = listener;
        this.userPlantDao = userPlantDao;

        taskIconMap = new HashMap<>();
        taskIconMap.put("Water", new Pair<>(R.drawable.water_drop_svgrepo_com, R.color.teal_700));
        taskIconMap.put("Rotate", new Pair<>(R.drawable.rotate_svgrepo_com, R.color.lightergreen));
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.US);
        holder.dueDate.setText(dateFormat.format(task.getDueDate()));

        if (task.isOverdue()) {
            float elevationInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6, holder.itemView.getResources().getDisplayMetrics());

            float radiusInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16, holder.itemView.getResources().getDisplayMetrics());

            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.soft_coral));
            holder.cardView.setCardElevation(elevationInPx);
            holder.cardView.setRadius(radiusInPx);
            holder.taskIcon.setImageResource(R.drawable.triangle_warning_svgrepo_com);
            holder.taskIcon.setImageTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.black));
        } else {
//            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.white));
        }

        new Thread(() -> {
            Log.d("TaskAdapter", "Fetching plant name for task ID: " + task.getId() + ", Plant ID: " + task.getUserPlantId());
            UserPlant plant = userPlantDao.getUserPlantById(task.getUserPlantId());
            String plantName = (plant != null) ? plant.getNickname() : "Unknown Plant";
            Log.d("TaskAdapter", "Fetched plant name: " + plantName + " for task ID: " + task.getId());

            holder.itemView.post(() -> holder.plantName.setText(plantName));
        }).start();

        Log.d("TaskAdapter", "Task: ID=" + task.getId() + ", Type=" + task.getTaskType() + ", DueDate=" + task.getDueDate());

        // Mark as Done button
        holder.markAsDoneButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskCompleted(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    // Method to get the icon resource and tint based on task type
    public Pair<Integer, Integer> getTaskIconAndTint(String taskType) {
        return taskIconMap.get(taskType); // Default if not found
    }



    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, dueDate, plantName;
        ImageButton markAsDoneButton;
        ImageView taskIcon;
        CardView cardView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            dueDate = itemView.findViewById(R.id.task_due_date);
            plantName = itemView.findViewById(R.id.task_plant_name);
            markAsDoneButton = itemView.findViewById(R.id.mark_as_done_button);
            taskIcon = itemView.findViewById(R.id.task_icon);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

    public interface OnTaskCompletedListener {
        void onTaskCompleted(Task task);
    }
}

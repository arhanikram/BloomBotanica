package com.example.bloombotanica.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bloombotanica.R;
import com.example.bloombotanica.database.UserPlantDao;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.models.UserPlant;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskCompletedListener listener;
    private UserPlantDao userPlantDao;

    public TaskAdapter(List<Task> tasks, OnTaskCompletedListener listener, UserPlantDao userPlantDao) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.listener = listener;
        this.userPlantDao = userPlantDao;
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
        holder.dueDate.setText(task.getDueDate().toString());

        if (task.isOverdue()) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.red_tint));
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
        tasks = newTasks != null ? newTasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, dueDate, plantName;
        ImageButton markAsDoneButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            dueDate = itemView.findViewById(R.id.task_due_date);
            plantName = itemView.findViewById(R.id.task_plant_name);
            markAsDoneButton = itemView.findViewById(R.id.mark_as_done_button);
        }
    }

    public interface OnTaskCompletedListener {
        void onTaskCompleted(Task task);
    }
}

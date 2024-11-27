package com.example.bloombotanica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.database.UserPlantDao;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private final UserPlantDao userPlantDao;

    public TaskListAdapter(List<Task> tasks, UserPlantDao userPlantDao) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.userPlantDao = userPlantDao;
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

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, plantName;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            plantName = itemView.findViewById(R.id.task_plant_name);
        }
    }
}

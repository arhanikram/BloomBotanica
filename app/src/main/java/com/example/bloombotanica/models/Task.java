package com.example.bloombotanica.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.bloombotanica.utils.DateUtils;

import java.util.Date;
import java.util.Objects;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userPlantId;
    private String taskType;
    private Date dueDate;
    private boolean isCompleted;

    public Task(int userPlantId, String taskType, Date dueDate, boolean isCompleted) {
        this.userPlantId = userPlantId;
        this.taskType = taskType;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
    }

    public boolean isOverdue() {
        return !isCompleted && dueDate.before(DateUtils.getStartOfDay(new Date()));
    }

    public String getTaskType() {
        return taskType;
    }
    public Date getDueDate() {
        return dueDate;
    }
    public boolean isCompleted() {
        return isCompleted;
    }

    public int getId() {
        return id;
    }
    public int getUserPlantId() {
        return userPlantId;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setUserPlantId(int userPlantId) {
        this.userPlantId = userPlantId;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public String toString() {
        return "Task{" + "id=" + id + ", userPlantId=" + userPlantId + ", taskType='" + taskType + '\'' + ", dueDate=" + dueDate + ", isCompleted=" + isCompleted + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id &&
                userPlantId == task.userPlantId &&
                dueDate.equals(task.dueDate) &&
                taskType.equals(task.taskType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userPlantId, dueDate, taskType);
    }

}

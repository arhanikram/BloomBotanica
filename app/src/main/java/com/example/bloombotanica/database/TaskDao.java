package com.example.bloombotanica.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bloombotanica.models.Task;

import java.util.Date;
import java.util.List;

@Dao
public interface TaskDao {

    @Insert()
    void insert(Task task);

    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startOfDay AND :endOfDay AND isCompleted = 0")
    List<Task> getTasksForDate(Date startOfDay, Date endOfDay);

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    List<Task> getIncompleteTasks();

    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :taskId")
    void markTaskAsCompleted(int taskId);

    @Query("DELETE FROM tasks WHERE userPlantId NOT IN (SELECT id FROM user_plants)")
    void removeTasksForDeletedPlants();

}


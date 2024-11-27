package com.example.bloombotanica.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bloombotanica.models.JournalEntry;

import java.util.List;

@Dao
public interface JournalEntryDao {
    @Insert
    void insert(JournalEntry entry);

    @Delete
    void delete(JournalEntry entry);

    @Query("SELECT * FROM journal_entries WHERE plant_id = :plantId ORDER BY timestamp DESC")
    List<JournalEntry> getEntriesForPlant(int plantId);

    @Query("SELECT * FROM journal_entries WHERE plant_id = :plantId AND care_type IS NOT NULL ORDER BY timestamp DESC")
    List<JournalEntry> getCareHistoryForPlant(int plantId);

    @Query("SELECT * FROM journal_entries WHERE plant_id = :plantId AND note IS NOT NULL ORDER BY timestamp DESC")
    List<JournalEntry> getUserLogsForPlant(int plantId);
}

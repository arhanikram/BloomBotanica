package com.example.bloombotanica.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.models.Task;
import com.example.bloombotanica.utils.Converters;
import com.example.bloombotanica.models.UserPlant;

@Database(entities = {UserPlant.class, Task.class, JournalEntry.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class UserPlantDatabase extends RoomDatabase {

    private static UserPlantDatabase instance;

    public abstract UserPlantDao userPlantDao();
    public abstract TaskDao taskDao();
    public abstract JournalEntryDao journalEntryDao();

    public static synchronized UserPlantDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext()
                    , UserPlantDatabase.class, "user_plant_database")
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }

}

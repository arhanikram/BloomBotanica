package com.example.bloombotanica.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
        Log.d("UserPlantDatabase", "getInstance Called");
        if (instance == null) {
            Log.d("UserPlantDatabase", "Creating new instance");
            instance = Room.databaseBuilder(context.getApplicationContext()
                    , UserPlantDatabase.class, "user_plant_database")
                    .fallbackToDestructiveMigration().addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Log.d("UserPlantDatabase", "Database created.");
                        }

                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            Log.d("UserPlantDatabase", "Database opened.");
                        }
                    }).build();
        }
        return instance;
    }

}

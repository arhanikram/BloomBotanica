package com.example.bloombotanica.models;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.util.Date;

@Entity(tableName = "journal_entries")
public class JournalEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "plant_id")
    private int plantId; // Foreign key to UserPlant
    @ColumnInfo(name = "timestamp")
    private Date timestamp;
    @ColumnInfo(name = "note")
    private String note;
    @ColumnInfo(name = "image_path")
    private String imagePath;
    @ColumnInfo(name = "care_type")
    private String careType;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) { this.plantId = plantId; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCareType() { return careType; }
    public void setCareType(String careType) { this.careType = careType; }
}

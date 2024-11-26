package com.example.bloombotanica.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "user_plants")
public class UserPlant {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int plantCareId;
    private String nickname;
    private Date dateAdded;  // Ensure a type converter is used for Date
    private Date lastWatered; // Ensure a type converter is used for Date
    @ColumnInfo(name = "image_path")
    private String imagePath; // New field to store the image path

    public UserPlant(int plantCareId, String nickname, Date dateAdded, Date lastWatered) {
        this.plantCareId = plantCareId;
        this.nickname = nickname;
        this.dateAdded = dateAdded;
        this.lastWatered = lastWatered;
    }

    // Getters
    public int getId() {
        return id;
    }
    public int getPlantCareId() {
        return plantCareId;
    }
    public String getNickname() {
        return nickname;
    }
    public Date getDateAdded() {
        return dateAdded;
    }
    public Date getLastWatered() {
        return lastWatered;
    }
    public String getImagePath() { return imagePath; }

    // Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setPlantCareId(int plantCareId) {
        this.plantCareId = plantCareId;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }
    public void setLastWatered(Date lastWatered) {
        this.lastWatered = lastWatered;
    }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}

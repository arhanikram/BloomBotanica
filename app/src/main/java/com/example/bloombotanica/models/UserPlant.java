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
    private Date nextWateringDate; // New field to store the next watering date
    private boolean isWatered; // New field to track if the plant has been watered
    private Date nextTurningDate; // New field to store the next turning date
    private Date lastTurned;

//    private boolean isNotificationEnabled; // IDEA - toggle notifications per userplant
    // (maybe user does not want notifs from plant A but only from plant B)


    public UserPlant(int plantCareId, String nickname, Date dateAdded) {
        this.plantCareId = plantCareId;
        this.nickname = nickname;
        this.dateAdded = dateAdded;
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

    public String getImagePath() {
        return imagePath;
    }

    public Date getNextWateringDate() {
        return nextWateringDate;
    }

    public Date getNextTurningDate() {
        return nextTurningDate;
    }

    public Date getLastTurned() {
        return lastTurned;
    }

    public boolean isWatered() {
        return isWatered;
    }

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

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setNextWateringDate(Date nextWateringDate) {
        this.nextWateringDate = nextWateringDate;
    }

    public void setNextTurningDate(Date nextTurningDate) {
        this.nextTurningDate = nextTurningDate;
    }

    public void setLastTurned(Date lastTurned) {
        this.lastTurned = lastTurned;
    }

    public void setWatered(boolean watered) {
        isWatered = watered;
    }

}

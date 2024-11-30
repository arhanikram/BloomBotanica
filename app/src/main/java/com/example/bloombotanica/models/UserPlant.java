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
    private int imageResource;

    public UserPlant(int plantCareId, String nickname, Date dateAdded, Date lastWatered, boolean isWatered, int imageResource) {
        this.plantCareId = plantCareId;
        this.nickname = nickname;
        this.dateAdded = dateAdded;
        this.lastWatered = lastWatered;
        this.isWatered = isWatered;
        this.imageResource = imageResource;
        this.imagePath = imagePath;
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

    public boolean isWatered() {
        return isWatered;
    }

    public int getImageResource() {  // Corrected getter method
        return imageResource;
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

    public void setWatered(boolean watered) {
        isWatered = watered;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}

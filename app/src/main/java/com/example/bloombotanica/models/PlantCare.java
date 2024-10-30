package com.example.bloombotanica.models;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plant_care")
public class PlantCare {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String plantName;
    private String scientificName;
    private int wateringFrequency; // in days
    private String sunlight; //Full sun, partial shade, etc
    private String soilType;

    public PlantCare(String plantName, String scientificName, int wateringFrequency, String sunlight, String soilType) {
        this.plantName = plantName;
        this.scientificName = scientificName;
        this.wateringFrequency = wateringFrequency;
        this.sunlight = sunlight;
        this.soilType = soilType;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public String getPlantName(){
        return plantName;
    }
    public String getScientificName(){
        return scientificName;
    }
    public int getWateringFrequency(){
        return wateringFrequency;
    }
    public String getSunlight() {
        return sunlight;
    }
    public String getSoilType() {
        return soilType;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
    public void setWateringFrequency(int wateringFrequency) {
        this.wateringFrequency = wateringFrequency;
    }
    public void setSunlight(String sunlight) {
        this.sunlight = sunlight;
    }
    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }
}

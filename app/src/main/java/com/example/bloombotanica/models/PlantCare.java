package com.example.bloombotanica.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plant_care")
public class PlantCare {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String commonName;
    private String scientificName;
    private int wateringFrequency; // in days
    private String sunlight; // Full sun, partial shade, etc.
    private String soilType;

    public PlantCare(String commonName, String scientificName, int wateringFrequency, String sunlight, String soilType) {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.wateringFrequency = wateringFrequency;
        this.sunlight = sunlight;
        this.soilType = soilType;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public int getWateringFrequency() {
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

    public void setCommonName(String commonName) {
        this.commonName = commonName;
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

    // Returns the name that best matches the query
    public String getCommonOrScientificName(String query) {
        if (commonName != null && commonName.toLowerCase().contains(query.toLowerCase())) {
            return commonName;
        } else if (scientificName != null && scientificName.toLowerCase().contains(query.toLowerCase())) {
            return scientificName;
        }
        return commonName != null ? commonName : scientificName;
    }

}

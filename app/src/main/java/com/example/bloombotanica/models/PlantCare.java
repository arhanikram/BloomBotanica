package com.example.bloombotanica.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "plant_care",
        indices = {@Index(value = {"commonName"}, unique = true),
                @Index(value = {"scientificName"}, unique = true)})
public class PlantCare implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String commonName;
    private String scientificName;
    private int wateringFrequency; // in days
    private String sunlight; // Full sun, partial shade, etc.
    private String soilType;
    private String plantDescription;

    public PlantCare(String commonName, String scientificName, int wateringFrequency, String sunlight, String soilType, String plantDescription) {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.wateringFrequency = wateringFrequency;
        this.sunlight = sunlight;
        this.soilType = soilType;
        this.plantDescription = plantDescription;
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

    public String getPlantDescription() {
        return plantDescription;
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

    public void setPlantDescription(String plantDescription) {
        this.plantDescription = plantDescription;
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

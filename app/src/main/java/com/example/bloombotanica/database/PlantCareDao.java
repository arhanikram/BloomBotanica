package com.example.bloombotanica.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.bloombotanica.models.PlantCare;

import java.util.List;

@Dao
public interface PlantCareDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(PlantCare plantCare);

    @Update
    void update(PlantCare plantCare);

    @Delete
    void delete(PlantCare plantCare);

    @Query("DELETE FROM plant_care")
    void deleteAll();  // Deletes all records in the table


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlantCare> plantCareList);

    @Query("SELECT * FROM plant_care ORDER BY id ASC")
    List<PlantCare> getAllPlants();

    @Query("SELECT * FROM plant_care WHERE commonName LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%'")
    List<PlantCare> searchPlants(String query);

    @Query("SELECT wateringFrequency FROM plant_care WHERE id = :plantCareId")
    int getWateringFrequencyById(int plantCareId);

    @Query("SELECT turningFrequency FROM plant_care WHERE id = :plantCareId")
    int getTurningFrequencyById(int plantCareId);

    @Query("SELECT * FROM plant_care WHERE id = :id")
    PlantCare getPlantCareById(int id);

    @Query("DELETE FROM plant_care WHERE commonName = :commonName")
    void deleteByCommonName(String commonName);

    @Query("SELECT * FROM plant_care WHERE commonName = :commonName")
    PlantCare getByCommonName(String commonName);

    @Query("DELETE FROM sqlite_sequence WHERE name = 'plant_care'")
    void resetSequence();
}

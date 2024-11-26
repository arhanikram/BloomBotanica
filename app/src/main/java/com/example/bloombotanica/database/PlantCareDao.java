package com.example.bloombotanica.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bloombotanica.models.PlantCare;

import java.util.List;

@Dao
public interface PlantCareDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlantCare plantCare);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PlantCare> plantCareList);

    @Query("SELECT * FROM plant_care")
    List<PlantCare> getAllPlants();

//    @Query("SELECT * FROM plant_care WHERE commonName LIKE '%' || :query || '%'")
//    List<PlantCare> searchByCommonName(String query);
//
//    @Query("SELECT * FROM plant_care WHERE scientificName LIKE '%' || :query || '%'")
//    List<PlantCare> searchByScientificName(String query);

    @Query("SELECT * FROM plant_care WHERE commonName LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%'")
    List<PlantCare> searchPlants(String query);

}

package com.example.bloombotanica.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.bloombotanica.models.UserPlant;

import java.util.List;

@Dao
public interface UserPlantDao {
    @Insert
    void insert(UserPlant userPlant);

    @Update
    void update(UserPlant userPlant);

    @Delete
    void delete(UserPlant userPlant);

    @Query("SELECT * FROM user_plants WHERE id = :id")
    UserPlant getUserPlantById(int id);

    @Query("SELECT * FROM user_plants")
    List<UserPlant> getAllUserPlants();

    @Query("UPDATE user_plants SET image_path = :imagePath WHERE id = :id")
    void updateImagePath(int id, String imagePath);

}

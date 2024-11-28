package com.example.bloombotanica.utils;

import androidx.room.TypeConverter;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromImageList(List<String> imagePaths) {
        if (imagePaths == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(imagePaths);  // Convert List<String> to JSON string
    }

    @TypeConverter
    public static List<String> toImageList(String imagePathsString) {
        if (imagePathsString == null) {
            return null;
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(imagePathsString, listType);  // Convert JSON string back to List<String>
    }
}

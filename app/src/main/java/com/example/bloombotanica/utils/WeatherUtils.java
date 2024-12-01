package com.example.bloombotanica.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherUtils {

    // Convert UNIX timestamp to a Date object
    public static Date convertUnixToDate(long unixTimestamp) {
        return new Date(unixTimestamp * 1000L);  // Multiply by 1000 to convert seconds to milliseconds
    }

    // Calculate light percentage based on current time, sunrise, and sunset
    public static double calculateLightPercentage(long sunriseUnix, long sunsetUnix) {
        // Get the current time
        long currentTime = System.currentTimeMillis() / 1000L;  // Current time in seconds

        // Convert sunrise and sunset times to Date
        Date sunrise = convertUnixToDate(sunriseUnix);
        Date sunset = convertUnixToDate(sunsetUnix);

        // Calculate the total daylight duration in seconds
        long daylightDuration = sunsetUnix - sunriseUnix;

        // Calculate how much of the day has passed
        long timeSinceSunrise = currentTime - sunriseUnix;

        // If it's before sunrise or after sunset, the light percentage is 0
        if (timeSinceSunrise < 0 || currentTime > sunsetUnix) {
            return 0.0;
        }

        // Calculate the light percentage (how much time has passed of the daylight duration)
        return (double) timeSinceSunrise / daylightDuration * 100;
    }

    // Format the date as a string for display
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
}

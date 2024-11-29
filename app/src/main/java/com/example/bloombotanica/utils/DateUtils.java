package com.example.bloombotanica.utils;

import android.util.Pair;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static Pair<Date, Date> getStartAndEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        // End of the day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calendar.getTime();

        return new Pair<>(startOfDay, endOfDay);
    }
}
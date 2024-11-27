package com.example.bloombotanica.utils;import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecurrenceUtils {

    public static List<Date> calculateFutureDates(Date startDate, int frequencyInDays, int monthsAhead) {
        List<Date> futureDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Add dates up to the specified number of months
        Date today = new Date();
        Calendar limit = Calendar.getInstance();
        limit.setTime(today);
        limit.add(Calendar.MONTH, monthsAhead);

        while (calendar.before(limit)) {
            futureDates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, frequencyInDays);
        }
        return futureDates;
    }
}

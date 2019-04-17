package com.saratms.risetodo.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sarah Al-Shamy on 30/03/2019.
 */

public class DateUtils {
    public static String getMonthFromDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM", Locale.ENGLISH); //Jun
        String monthString = simpleDateFormat.format(date);
        return monthString;
    }

    public static String getLargeMonthFromDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH); //Jun
        String monthString = simpleDateFormat.format(date);
        return monthString;
    }

    public static String getDayFromDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd", Locale.ENGLISH); //20
        String day = simpleDateFormat.format(date);
        return day;
    }

    public static Date getDateFromString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return convertedDate;
    }

    public static Calendar convertMillisecondsToCalendar(String milliseconds){
        Calendar calendar = Calendar.getInstance();

        //convert the String milliseconds to Long variable so we can convert it into calendar
        Long millisecondsLong = Long.parseLong(milliseconds);
        calendar.setTimeInMillis(millisecondsLong);
        return calendar;
    }

    public static String formatReminderTime(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd MMMM, yyyy  hh:mm a");
        return simpleDateFormat.format(calendar.getTime());
    }

}

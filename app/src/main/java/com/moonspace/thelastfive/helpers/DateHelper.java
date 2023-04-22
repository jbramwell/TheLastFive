package com.moonspace.thelastfive.helpers;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateHelper
{
    // The common date format used throughout this app
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    // The common time format used throughout this app
    private static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * Returns a formatted date string given a Date value.
     *
     * @param date the Date value to format.
     * @return a formatted date string.
     */
    public static String getFormattedDate(Date date)
    {
        String formattedDateString;

        android.icu.text.SimpleDateFormat dateFormat = new SimpleDateFormat(DateHelper.DATE_FORMAT);
        formattedDateString = dateFormat.format(date);

        return formattedDateString;
    }

    /**
     * Returns a formatted time string given a Date value.
     *
     * @param date the Date value to format.
     * @return a formatted time string.
     */
    public static String getFormattedTime(Date date)
    {
        String formattedDateString;

        android.icu.text.SimpleDateFormat dateFormat = new SimpleDateFormat(DateHelper.TIME_FORMAT);
        formattedDateString = dateFormat.format(date);

        return formattedDateString;
    }

    /**
     * Returns a formatted date string given a year, month and day.
     *
     * @param year  the year of the date.
     * @param month the month of the date (as a number).
     * @param day   the day of the date (as a number).
     * @return a formatted date string.
     */
    public static String getFormattedDate(int year, int month, int day)
    {
        String formattedDateString = String.format("%s-%s-%s", year, month + 1, day);

        return getFormattedDate(getDateFromFormattedString(formattedDateString));
    }

    /**
     * Returns a Date value based on a formatted date string.
     *
     * @param formattedDateString the formatted date string to parse.
     * @return a Date value based on a formatted date string.
     */
    public static Date getDateFromFormattedString(String formattedDateString)
    {
        Date convertedDate = null;

        android.icu.text.SimpleDateFormat dateFormat = new SimpleDateFormat(DateHelper.DATE_FORMAT);

        try
        {
            convertedDate = dateFormat.parse(formattedDateString);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return convertedDate;
    }

    /**
     * Returns the current date.
     *
     * @return the current date.
     */
    public static Date getCurrentDate()
    {
        return Calendar.getInstance().getTime();
    }

    /**
     * Returns the current date as a formatted string.
     *
     * @return the current date as a formatted string.
     */
    public static String getCurrentFormattedDate()
    {
        Calendar calendar = Calendar.getInstance();

        return DateHelper.getFormattedDate(calendar.getTime());
    }

    /**
     * Returns the number of days between now and another date.
     *
     * @param endDate the end date to count the days between.
     * @return the number of days between now and another date.
     */
    public static Double getDaysFromNow(Date endDate)
    {
        return getDaysBetweenDates(Calendar.getInstance().getTime(), endDate);
    }

    /**
     * Returns the number of days between two dates.
     *
     * @param startDate the start  date to count the days between.
     * @param endDate   the end date to count the days between.
     * @return the number of days between two dates.
     */
    public static Double getDaysBetweenDates(Date startDate, Date endDate)
    {
        long diffInMilliseconds = endDate.getTime() - startDate.getTime();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMilliseconds);

        return (double) diffInDays;
    }
}

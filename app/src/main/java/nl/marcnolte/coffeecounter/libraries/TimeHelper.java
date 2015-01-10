package nl.marcnolte.coffeecounter.libraries;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

final public class TimeHelper
{
    /**
     * Debug Tag
     */
    final private static String DEBUG_TAG = "TimeHelper";

    public static int getOffset()
    {
        Calendar mCalendar  = Calendar.getInstance();
        TimeZone mTimezone  = TimeZone.getDefault();
        Date     mLocalTime = mCalendar.getTime();

        return mTimezone.getOffset(mLocalTime.getTime()) / 1000;
    }

    public static String formatDate(String dateStr, DateFormat dateFormat)
    {
        try
        {
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date             mDate   = mFormat.parse(dateStr);
            return dateFormat.format(mDate);
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatTime(String timeStr, DateFormat dateFormat)
    {
        try
        {
            SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");
            Date             mTime   = mFormat.parse(timeStr);
            return dateFormat.format(mTime);
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDate(String timezone)
    {
        // Set local (device) time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar mLocalTime  = new GregorianCalendar();

        // Set timezone
        TimeZone mTimezone;
        switch(timezone)
        {
            // Local (device) timezone
            case "device":
            case "local":
                mTimezone = TimeZone.getDefault();
                break;

            // Set from parameter
            default:
                mTimezone = TimeZone.getTimeZone(timezone);
        }
        sdf.setTimeZone(mTimezone);

        // Return date in requested timezone
        return sdf.format(mLocalTime.getTimeInMillis());
    }

    public static String getDatetime(String type, String timezone)
    {
        // Set local (device) time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar mLocalTime  = new GregorianCalendar();

        // Set date
        switch(type)
        {
            // Start of the day
            case "sod" :
                mLocalTime.set(Calendar.HOUR_OF_DAY, 0);
                mLocalTime.set(Calendar.MINUTE, 0);
                mLocalTime.set(Calendar.SECOND, 0);
                mLocalTime.set(Calendar.MILLISECOND, 0);
                break;

            // End of the day
            case "eod":
                mLocalTime.set(Calendar.HOUR_OF_DAY, 23);
                mLocalTime.set(Calendar.MINUTE, 59);
                mLocalTime.set(Calendar.SECOND, 59);
                mLocalTime.set(Calendar.MILLISECOND, 999);
                break;

            // Now
            case "now":
            default:
                // Do nothing for current time
        }

        // Set timezone
        TimeZone mTimezone;
        switch(timezone)
        {
            // Local (device) timezone
            case "device":
            case "local":
                mTimezone = TimeZone.getDefault();
                break;

            // Set from parameter
            default:
                mTimezone = TimeZone.getTimeZone(timezone);
        }
        sdf.setTimeZone(mTimezone);

        // Return datetime in requested timezone
        return sdf.format(mLocalTime.getTimeInMillis());
    }

    public static boolean isToday(String dateStr)
    {
        try
        {
            SimpleDateFormat sdf   = new SimpleDateFormat("yyyy-MM-dd");
            Date             mDate = sdf.parse(dateStr);
            return (new Date().after(mDate));
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static String removeTime(String datetimeStr)
    {
        return datetimeStr.split(" ")[0];
    }
}

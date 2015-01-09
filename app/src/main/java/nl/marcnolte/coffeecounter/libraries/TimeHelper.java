package nl.marcnolte.coffeecounter.libraries;

import android.util.Log;

import java.text.DateFormat;
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
        catch(Exception e)
        {
            Log.e(DEBUG_TAG, e.getMessage());
            return dateStr;
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
        catch(Exception e)
        {
            Log.e(DEBUG_TAG, e.getMessage());
            return timeStr;
        }
    }

    public static String getDatetime(String type, String timezone)
    {
        // Prep parameters
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar mLocalTime  = new GregorianCalendar();
        TimeZone mTimezone;

        // Set time for date
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
            default:
                // Do nothing
        }

        // Set timezone
        if (timezone == "local") {
            mTimezone = TimeZone.getDefault();
        } else {
            mTimezone = TimeZone.getTimeZone("UTC");
        }
        sdf.setTimeZone(mTimezone);

        // Return formatted datetime in UTC
        return sdf.format(mLocalTime.getTimeInMillis());
    }
}

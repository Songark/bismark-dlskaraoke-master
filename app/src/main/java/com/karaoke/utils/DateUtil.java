package com.karaoke.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private static final int HOUR_IN_SECONDS = 3600;
    private static final int HOUR_IN_MINUTES = 60;
    public static final String DATE_FORMAT_1 = "MM/dd/yyyy";
    public static final String DATE_FORMAT_2 = "MM/dd/yyyy HH:mm";
    public static final String DATE_FORMAT_3 = "MMM./dd/yyyy";
    public static final String DATE_FORMAT_4 = "dd/MM/yyyy";
    public static final String DATE_FORMAT_5 = "dd-MMM-yyyy";
    public static final String DATE_FORMAT_6 = "MMM-yyyy";
    public static final String DATE_FORMAT_7 = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_8 = "EEE, MMM d, h:mm a"; 	// Tue, Nov 24, 10:58 AM
    public static final String DATE_FORMAT_9 = "EEE, d MMM yyyy"; 		// Thu, 15 Apr 2015
    public static final String DATE_FORMAT_10 = "h:mm a"; 				// 08:00 AM
    
    /**
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * toString for format 1.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_1(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_1);
	}
	
	/**
	 * toString for format 2.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_2(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_2);
	}

	
	/**
	 * toString for format 3.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_3(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_3);
	}

	/**
	 * toString for format 4.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_4(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_4);
	}	
	
	/**
	 * toString for format 5.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_5(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_5);
	}
	
	/**
	 * toString for format 6.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_6(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_6);
	}
	
	/**
	 * toString for format 7.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_7(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_7);
	}
	
	/**
	 * toString for format 8.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_8(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_8);
	}
	
	/**
	 * toString for format 9.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_9(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_9);
	}
	
	/**
	 * toString for format 10.
	 * @param date
	 * @return
	 */
	public static String toStringFormat_10(Date date) {
		if (date == null)
			return "";
		return dateToString(date, DATE_FORMAT_10);
	}
	
	public static Object getSOAPDateString(java.util.Date itemValue) {
	    String lFormatTemplate = "yyyy-MM-dd'T'hh:mm:ss'Z'";
	    DateFormat lDateFormat = new SimpleDateFormat(lFormatTemplate);
	    String lDate = lDateFormat.format(itemValue);
	    return lDate;
	}
	
    public static Calendar getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();
        long currentTimeMillis = System.currentTimeMillis();
        int day = getDayFromTimestamp(currentTimeMillis);
        int month = getMonthFromTimestamp(currentTimeMillis);
        int year = getYearFromTimestamp(currentTimeMillis);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        return calendar;
    }

    public static long getTimestamp(int year, int month, int day)
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }

    public static int getYearFromTimestamp(long timestamp)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        return c.get(Calendar.YEAR);
    }

    public static int getMonthFromTimestamp(long timestamp)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        return c.get(Calendar.MONTH);
    }

    public static int getHourFromTimestamp(long timestamp)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinuteFromTimestamp(long timestamp)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        return c.get(Calendar.MINUTE);
    }

    public static int getSecondFromTimestamp(long timestamp)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        return c.get(Calendar.SECOND);
    }

    public static String getFormattedDateFromTimestamp(long timestamp)
    {
        int year = getYearFromTimestamp(timestamp);
        int month = getMonthFromTimestamp(timestamp);
        int day = getDayFromTimestamp(timestamp);

        return year + " - " + (month + 1) + " - " + day;
    }
    
    public static String getFormattedDate2FromTimestamp(long timestamp)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return toStringFormat_1(cal.getTime());
    }

    public static int getDayFromTimestamp(long timestamp)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        return c.get(Calendar.DAY_OF_MONTH);
    }

    public static Calendar getCalendarFromTimestamp(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        int day = getDayFromTimestamp(timestamp);
        int month = getMonthFromTimestamp(timestamp);
        int year = getYearFromTimestamp(timestamp);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        return calendar;
    }

    public static String getCompleteFormattedDateFromTimestamp(long timestamp)
    {
        int year = getYearFromTimestamp(timestamp);
        int month = getMonthFromTimestamp(timestamp);
        int day = getDayFromTimestamp(timestamp);
        int hour = getHourFromTimestamp(timestamp);
        int minute = getMinuteFromTimestamp(timestamp);
        int second = getSecondFromTimestamp(timestamp);

        return year + " - " + String.format("%02d", (month + 1)) + " - " + String.format("%02d", day) + " "
            + String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
    }
    
    public static Date parseDataFromFormat7(String dateString) {
    	Date retDate = new Date();
    	SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_7);  
    	try {  
    	    retDate = format.parse(dateString);  
    	} catch (ParseException e) {  
    	    e.printStackTrace();  
    	}
    	return retDate;
    }
}

package com.example.mytodo_app.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class CommonUtils {
  /**
   * Return date in specified format.
   * @param milliSeconds Date in milliseconds
   * @param dateFormat Date format 
   * @return String representing date in specified format
   */
  public static String getStringDate(long milliSeconds, String dateFormat)
  {
      // Create a DateFormatter object for displaying date in specified format.
      DateFormat formatter = new SimpleDateFormat(dateFormat);

      // Create a calendar object that will convert the date and time value in milliseconds to date. 
       Calendar calendar = Calendar.getInstance();
       calendar.setTimeInMillis(milliSeconds);
       Log.d("CommonUtils", formatter.format(calendar.getTime()));
       return formatter.format(calendar.getTime());
  }
  
  public static String getStringDate(Calendar calendar, String dateFormat)
  {
      // Create a DateFormatter object for displaying date in specified format.
      DateFormat formatter = new SimpleDateFormat(dateFormat);
      return formatter.format(calendar.getTime());
  }
  
  
  public static Date getDate(String strDate, String dateFormat) {
    // Create a DateFormatter object for displaying date in specified format.
    DateFormat formatter = new SimpleDateFormat(dateFormat);
    
    try {
      return formatter.parse(strDate);
    } catch (ParseException e) {
      e.printStackTrace();
      return new Date();
    }
  }
  
}

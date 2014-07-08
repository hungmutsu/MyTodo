package com.example.mytodo_app.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class CommonUtils {

  /**
   * Return string date in specified format
   * 
   * @param calendar
   * @param dateFormat
   *          Date format
   * @return
   */
  public static String getStringDate(Calendar calendar, String dateFormat) {
    // Create a DateFormatter object for displaying date in specified format.
    DateFormat formatter = new SimpleDateFormat(dateFormat);
    return formatter.format(calendar.getTime());
  }

  /**
   * Return date in specified format
   * 
   * @param strDate
   * @param dateFormat
   * @return
   */
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

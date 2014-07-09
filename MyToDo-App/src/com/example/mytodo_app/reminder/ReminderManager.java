package com.example.mytodo_app.reminder;

import java.util.Calendar;

import com.example.mytodo_app.provider.MyToDo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderManager {
  private static final String TAG = "ReminderManager";
  private AlarmManager mAlarmManager;
  private Context mContext;

  public ReminderManager(Context context) {
    // TODO Auto-generated constructor stub
    mContext = context;
    mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  }

  public void setReminder(Long taskId, Calendar calendar, String name, String description) {
    Log.d("==ReminderManager==", "==setReminder==");
    Intent i = new Intent(mContext, OnAlarmReceiver.class);
    i.putExtra(MyToDo.Tasks._ID, (long) taskId);
    i.putExtra(MyToDo.Tasks.COLUMN_NAME_NAME, name);
    i.putExtra(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION, description);
    
    Log.i(TAG, "taskId : " + taskId + "name : " + name + "description : " + description);
    
    PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);
    mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);

  }

  public void setUpdateReminder(Long taskId, Calendar calendar, String name) {
    Log.d("==ReminderManager==", "==setUpdateReminder==");
    Intent i = new Intent(mContext, OnAlarmReceiver.class);
    i.putExtra(MyToDo.Tasks._ID, (long) taskId);
    i.putExtra(MyToDo.Tasks.COLUMN_NAME_NAME, name);
    
    Log.i(TAG, "taskId : " + taskId + "name : " + name);
    
    PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
  }

}

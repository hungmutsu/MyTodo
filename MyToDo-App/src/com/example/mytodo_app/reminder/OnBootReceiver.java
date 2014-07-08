package com.example.mytodo_app.reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.mytodo_app.provider.MyToDo;
import com.example.mytodo_app.utils.Constant;

public class OnBootReceiver extends BroadcastReceiver {

  private static final String[] TASK_PROJECTION = new String[] { MyToDo.Tasks._ID, MyToDo.Tasks.COLUMN_NAME_ID,
      MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION, MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE,
      MyToDo.Tasks.COLUMN_NAME_CREATE_DATE, MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE };

  @SuppressLint("SimpleDateFormat")
  @Override
  public void onReceive(Context context, Intent intent) {
    // TODO Auto-generated method stub
    Log.d("==OnBootReceiver==", "==onReceive==");
    ReminderManager reminderMgr = new ReminderManager(context);

    Cursor cursor = context.getContentResolver().query(MyToDo.Tasks.CONTENT_URI, TASK_PROJECTION, null, null, null);
    if (cursor != null) {
      cursor.moveToFirst();
      int rowIdColumnIndex = cursor.getColumnIndex(MyToDo.Tasks._ID);
      int dateTimeColumnIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE);
      int titleColumnIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_NAME);
      int bodyColumnIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION);
      while (cursor.isAfterLast() == false) {
        Long rowId = cursor.getLong(rowIdColumnIndex);
        String dateTime = cursor.getString(dateTimeColumnIndex);
        String title = cursor.getString(titleColumnIndex);
        String body = cursor.getString(bodyColumnIndex);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(Constant.DATE_TIME_FORMAT);
        try {
          java.util.Date date = format.parse(dateTime);
          cal.setTime(date);
          reminderMgr.setReminder(rowId, cal, title, body);
        } catch (Exception e) {
          Log.e("OnBootReceiver", e.getMessage(), e);
        }
        cursor.moveToNext();
      }
      cursor.close();
    }
  }

}

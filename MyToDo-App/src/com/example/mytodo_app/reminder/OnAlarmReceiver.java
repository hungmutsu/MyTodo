package com.example.mytodo_app.reminder;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.mytodo_app.provider.MyToDo;
import com.example.mytodo_app.utils.CommonUtils;
import com.example.mytodo_app.utils.Constant;

public class OnAlarmReceiver extends BroadcastReceiver {

  private Context mContext;
  MediaPlayer mediaPlayer;

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.d("==OnAlarmReceiver==", "==onReceive==");
    // TODO Auto-generated method stub
    this.mContext = context;
    final long rowid = intent.getExtras().getLong(MyToDo.Tasks._ID);
    final String name = intent.getExtras().getString(MyToDo.Tasks.COLUMN_NAME_NAME);
    final String description = intent.getExtras().getString(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION);
    WakeReminderIntentService.acquireStaticLock(context);
    Intent i = new Intent(context, ReminderService.class);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.putExtra(MyToDo.Tasks._ID, rowid);
    i.putExtra(MyToDo.Tasks.COLUMN_NAME_NAME, name);

    // thong bao reminder bang sound
    playMedia(context);
    // hien thi dialog reminder
    showAlarmDialog(context, rowid, name, description);

    context.startService(i);
  }

  private void playMedia(Context context) {
    try {
      Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mediaPlayer.setDataSource(context, uri);
      mediaPlayer.prepare();
      mediaPlayer.setLooping(true);
      mediaPlayer.start();
    } catch (Exception e) {
      Log.d("==ERROR==", "" + e);
    }
  }

  private void showAlarmDialog(Context context, final long rowid, final String title, final String body) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("Reminder");
    builder.setMessage(title);
    builder.setNegativeButton("Sleep", new OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        setReminder(rowid, title, body);
        mediaPlayer.stop();
        Log.d("==Sleep==", "SLEEP" + rowid);
      }
    });
    builder.setPositiveButton("Cancel", new OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        mediaPlayer.stop();
        // cancel regular alarms
        PendingIntent pi = getPendingIntent(mContext, (int) rowid);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
        pi.cancel();
      }
    });
    AlertDialog alertDialog = builder.create();
    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    alertDialog.show();
  }

  @SuppressLint("SimpleDateFormat")
  protected void setReminder(long rowid, String name, String description) {
    Calendar mCalendar = Calendar.getInstance();
    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1);
    int minutes = mCalendar.get(Calendar.MINUTE) + 5;
    mCalendar.set(Calendar.MINUTE, minutes);

    Log.d("==SLEEP==",
        mCalendar.get(Calendar.YEAR) + "-" + mCalendar.get(Calendar.MONTH) + "/" + mCalendar.get(Calendar.DAY_OF_MONTH)
            + "|" + mCalendar.get(Calendar.HOUR_OF_DAY) + ":" + minutes);

    // Sets up a map to contain values to be updated in the provider.
    ContentValues values = new ContentValues();

    values.put(MyToDo.Tasks.COLUMN_NAME_NAME, name);
    values.put(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION, description);
    values.put(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, CommonUtils.getStringDate(mCalendar, Constant.DATE_TIME_FORMAT));
    ;
    values.put(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE,
        CommonUtils.getStringDate(Calendar.getInstance(), Constant.DATE_TIME_FORMAT));

    Uri mUri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_ID_URI_BASE, String.valueOf(rowid));
    int count = mContext.getContentResolver().update(mUri, values, null, null);

    if (count > 0) {
      new ReminderManager(mContext).setUpdateReminder(rowid, mCalendar, name);

      Toast.makeText(mContext, name + " Reminder Snoozed for " + 2 + " Minutes ", Toast.LENGTH_LONG).show();
    }
  }

  // get a PendingIntent
  PendingIntent getPendingIntent(Context context, int id) {
    Intent intent = new Intent(context, OnAlarmReceiver.class).putExtra(MyToDo.Tasks._ID, id);
    return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }
}

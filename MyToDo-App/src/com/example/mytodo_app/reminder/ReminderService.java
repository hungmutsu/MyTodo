package com.example.mytodo_app.reminder;

import com.example.mytodo_app.R;
import com.example.mytodo_app.TaskEditorActivity;
import com.example.mytodo_app.provider.MyToDo;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class ReminderService extends WakeReminderIntentService {
  public ReminderService() {
    super("ReminderService");
  }

  @Override
  void doReminderWork(Intent intent) {
    Log.d("==ReminderService==", "==doReminderWork==");
    // TODO Auto-generated method stub
    Long rowId = intent.getExtras().getLong(MyToDo.Tasks._ID);
    NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    Intent notificationIntent = new Intent(this, TaskEditorActivity.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    notificationIntent.putExtra(MyToDo.Tasks._ID, rowId);
    PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
    Notification note = new Notification(android.R.drawable.stat_sys_warning,
        getString(R.string.notify_new_task_message), System.currentTimeMillis());
    note.setLatestEventInfo(this, getString(R.string.notifiy_new_task_title),
        getString(R.string.notify_new_task_message), pi);
    note.defaults |= Notification.DEFAULT_SOUND;
    note.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
    int id = (int) ((long) rowId);

    mgr.notify(id, note);
    /*
     * AlertDialog.Builder builder = new AlertDialog.Builder(this); builder.setTitle("reminder");
     * builder.setMessage("message"); builder.setNegativeButton("repeat", null); builder.setPositiveButton("repeat",
     * null); AlertDialog alertDialog = builder.create();
     * alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); alertDialog.show();
     */
    // Toast.makeText(getApplicationContext(), "Alarm Triggered and SMS Sent", Toast.LENGTH_LONG).show();
  }

}

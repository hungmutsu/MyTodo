package com.example.mytodo_app.reminder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class WakeReminderIntentService extends IntentService {

  abstract void doReminderWork(Intent intent);

  public static final String LOCK_NAME_STATIC = "com.example.mytodo_app.reminder.static";
  private static PowerManager.WakeLock lockStatic = null;

  public static void acquireStaticLock(Context context) {
    getLock(context).acquire();
  }

  public WakeReminderIntentService(String name) {
    super(name);
    // TODO Auto-generated constructor stub
  }

  synchronized private static PowerManager.WakeLock getLock(Context context) {
    if (lockStatic == null) {
      PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

      lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
      lockStatic.setReferenceCounted(true);
    }

    return (lockStatic);
  }

  @Override
  final protected void onHandleIntent(Intent intent) {
    try {
      doReminderWork(intent);
    } finally {
      getLock(this).release();
    }
  }
}

package com.example.mytodo_app.syncadapter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.example.mytodo_app.provider.MyToDo;
import com.example.mytodo_app.utils.NetworkUtils;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

  private static final String TAG = "SyncAdapter";
  private static final String URL_HOST = "http://192.168.1.77/mytodo-service/";
  // JSON Node names
  public static final String TAG_ERROR = "error";
  private static final String[] TASK_PROJECTION = new String[] { MyToDo.Tasks._ID, MyToDo.Tasks.COLUMN_NAME_ID,
      MyToDo.Tasks.COLUMN_NAME_USER_ID, MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION,
      MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, MyToDo.Tasks.COLUMN_NAME_CREATE_DATE,
      MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE, MyToDo.Tasks.COLUMN_NAME_IS_DRAFT };

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void onPerformSync(Account account, Bundle bundle, String authority,
      ContentProviderClient contentProviderClient, SyncResult syncResult) {
    Log.i(TAG, "starting sync");

    String updatedDate = getUpdatedDate(contentProviderClient);
    try {
      syncToLocal(account, contentProviderClient, updatedDate);
      syncToServer(account, contentProviderClient);
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log.i(TAG, "done sync");

  }

  /**
   * @param contentProviderClient
   * @throws RemoteException
   */
  public void syncToLocal(Account account, ContentProviderClient contentProviderClient, String updated)
      throws RemoteException {
    Log.i(TAG, "begin sync to local");
    String urlGetAllTask = URL_HOST + "get-all-task.php";

    // Building Parameters
    String[] keys = new String[] { "username", "updatedDate" };
    String[] values = new String[] { account.name, updated };

    try {
      Log.i(TAG, "Last updatedDate : " + updated);
      JSONObject json = NetworkUtils.postJSONObjFromUrl(urlGetAllTask, keys, values);

      // check your log for json response
      Log.d("All task server result", json.toString());

      // json success tag
      boolean error = json.getBoolean(TAG_ERROR);

      if (!error) {

        JSONArray arrTask = json.getJSONArray("task");
        for (int i = 0; i < arrTask.length(); i++) {

          ContentValues contentValues = new ContentValues();
          JSONObject jsonObject = arrTask.getJSONObject(i);

          contentValues.put(MyToDo.Tasks.COLUMN_NAME_ID, jsonObject.getInt(MyToDo.Tasks.COLUMN_NAME_ID));
          contentValues.put(MyToDo.Tasks.COLUMN_NAME_NAME, jsonObject.getString(MyToDo.Tasks.COLUMN_NAME_NAME));
          contentValues.put(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION,
              jsonObject.getString(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION));

          String reminder = jsonObject.getString(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE);
          String createdDate = jsonObject.getString(MyToDo.Tasks.COLUMN_NAME_CREATE_DATE);
          String updatedDate = jsonObject.getString(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE);

          if (StringUtils.isNotBlank(reminder) && !"0000-00-00 00:00:00".equals(reminder)) {
            contentValues.put(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, reminder);
          }
          if (StringUtils.isNotBlank(createdDate)) {
            contentValues.put(MyToDo.Tasks.COLUMN_NAME_CREATE_DATE, createdDate);
          }
          if (StringUtils.isNotBlank(updatedDate)) {
            contentValues.put(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE, updatedDate);
          }

          contentValues.put(MyToDo.Tasks.COLUMN_NAME_IS_DRAFT, 1);

          // Kiểm tra tồn tại task?
          Uri uri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_SERVER_ID_URI_BASE,
              jsonObject.getString(MyToDo.Tasks.COLUMN_NAME_ID));
          Cursor cursor = contentProviderClient.query(uri, TASK_PROJECTION, null, null, null);

          if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.i(TAG, "Count Cursor : " + cursor.getCount());
            // Local taskId
            int id = cursor.getInt(cursor.getColumnIndex(MyToDo.Tasks._ID));
            // Thực hiện update
            Uri updateUri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_ID_URI_BASE, String.valueOf(id));
            Log.i(TAG, updateUri.toString());
            contentProviderClient.update(updateUri, contentValues, null, null);
          } else {
            // Thực hiện insert
            Log.i(TAG, "Inserting task");
            contentProviderClient.insert(MyToDo.Tasks.CONTENT_URI, contentValues);
            Log.i(TAG, "Inserted task");
          }
        }

      } else {
        Log.i(TAG, json.getString("message"));

      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    Log.i(TAG, "end sync to local");
  }

  /**
   * @param contentProviderClient
   * @return
   */
  private String getUpdatedDate(ContentProviderClient contentProviderClient) {
    try {
      Uri uri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_DRAP_URI_BASE, "1");
      Cursor cursor = contentProviderClient.query(uri, TASK_PROJECTION, null, null, "_ID DESC LIMIT(1)");
      if (cursor.getCount() > 0) {
        cursor.moveToFirst();

        int colUpdateIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE);
        return cursor.getString(colUpdateIndex);
      }
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }

  /**
   * @param contentProviderClient
   */
  private void syncToServer(Account account, ContentProviderClient contentProviderClient) {
    Log.i(TAG, "begin sync to server");
    String urlAddTask = URL_HOST + "add-task.php";
    // Building Parameters
    String[] keys = new String[] { "username", "name", "description", "reminderDate", "createdDate", "updatedDate" };

    try {
      Uri uri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_DRAP_URI_BASE, "0");
      Cursor cursor = contentProviderClient.query(uri, TASK_PROJECTION, null, null, null);
      Log.i(TAG, "Uri query draf : " + uri.toString());

      if (cursor.getCount() > 0) {
        Log.i(TAG, "Count cursor : " + cursor.getCount());
        cursor.moveToFirst();
        int colIdIndex = cursor.getColumnIndex(MyToDo.Tasks._ID);
        // int colTaskIdIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_ID);
        int colUserIdIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_USER_ID);
        int colNameIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_NAME);
        int colDescriptionIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION);
        int colReminderIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE);
        int colCreatedDateIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_CREATE_DATE);
        int colUpdatedDateIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE);

        do {
          int id = cursor.getInt(colIdIndex);
          int userId = cursor.getInt(colUserIdIndex);
          String name = cursor.getString(colNameIndex);
          String description = cursor.getString(colDescriptionIndex);
          String reminderDate = cursor.getString(colReminderIndex);
          String createdDate = cursor.getString(colCreatedDateIndex);
          String updatedDate = cursor.getString(colUpdatedDateIndex);

          String[] values = new String[] { account.name, name, description, reminderDate, createdDate, updatedDate };

          JSONObject json = NetworkUtils.postJSONObjFromUrl(urlAddTask, keys, values);
          // check your log for json response
          Log.d("task add result", json.toString());

          // json success tag
          boolean error = json.getBoolean(TAG_ERROR);

          if (!error) {
            JSONArray arrTask = json.getJSONArray("task");
            for (int i = 0; i < arrTask.length(); i++) {

              ContentValues contentValues = new ContentValues();
              JSONObject jsonObject = arrTask.getJSONObject(i);

              contentValues.put(MyToDo.Tasks.COLUMN_NAME_ID, jsonObject.getInt(MyToDo.Tasks.COLUMN_NAME_ID));
              contentValues.put(MyToDo.Tasks.COLUMN_NAME_IS_DRAFT, 1);
              Log.i(TAG, "Updating task : " + id);

              Uri updateUri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_ID_URI_BASE, String.valueOf(id));
              contentProviderClient.update(updateUri, contentValues, null, null);
              Log.i(TAG, "Updated task");
            }

          } else {
            Log.i(TAG, json.getString("message"));
          }
        } while (cursor.moveToNext());

      }
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log.i(TAG, "end sync to server");
  }

}

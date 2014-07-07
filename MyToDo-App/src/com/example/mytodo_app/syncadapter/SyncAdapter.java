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
      insertTask(account, contentProviderClient, updatedDate);
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log.i(TAG, updatedDate);

    Log.i(TAG, "done sync");

  }

  /**
   * @param contentProviderClient
   * @throws RemoteException
   */
  public void insertTask(Account account, ContentProviderClient contentProviderClient, String updated)
      throws RemoteException {
    String urlGetAllTask = URL_HOST + "get-all-task.php";

    // Building Parameters
    String[] keys = new String[] { "username", "updatedDate" };
    String[] values = new String[] { account.name, updated };

    try {

      JSONObject json = NetworkUtils.postJSONObjFromUrl(urlGetAllTask, keys, values);

      // check your log for json response
      Log.d("All task result", json.toString());

      // json success tag
      boolean error = json.getBoolean(TAG_ERROR);

      Log.i(TAG, String.valueOf(error));
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
          
          
          Log.i(TAG, "Inserting task");
          contentProviderClient.insert(MyToDo.Tasks.CONTENT_URI, contentValues);
          Log.i(TAG, "Inserted task");

        }

      } else {
        Log.i(TAG, json.getString("message"));

      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param contentProviderClient
   * @return
   */
  private String getUpdatedDate(ContentProviderClient contentProviderClient) {
    String[] TASK_PROJECTION = new String[] { MyToDo.Tasks._ID, MyToDo.Tasks.COLUMN_NAME_ID,
        MyToDo.Tasks.COLUMN_NAME_USER_ID, MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION,
        MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, MyToDo.Tasks.COLUMN_NAME_CREATE_DATE,
        MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE, MyToDo.Tasks.COLUMN_NAME_IS_DRAFT };

    try {
      Uri uri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_DRAP_URI_BASE, "1");
      Log.i(TAG, "URI : " + uri);
      Cursor cursor = contentProviderClient.query(uri, TASK_PROJECTION, null, null, "_ID DESC LIMIT(1)");
      if (cursor.getCount() > 0) {
        cursor.moveToFirst();

        int colIdIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_ID);
        Log.i(TAG, "ID = " + cursor.getInt(colIdIndex));
        int colUpdateIndex = cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE);
        return cursor.getString(colUpdateIndex);
      }
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }

}

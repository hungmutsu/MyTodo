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
import com.example.mytodo_app.utils.Constant;
import com.example.mytodo_app.utils.NetworkUtils;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

  private static final String TAG = "SyncAdapter";
  private static final String URL_HOST = "http://192.168.1.77/mytodo-service/";
  // JSON Node names
  public static final String TAG_ERROR = "error";
  private static final String[] TASK_PROJECTION = new String[] { MyToDo.Tasks._ID, MyToDo.Tasks.COLUMN_NAME_ID,
      MyToDo.Tasks.COLUMN_NAME_USER_NAME, MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION,
      MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, MyToDo.Tasks.COLUMN_NAME_CREATE_DATE,
      MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE };

  private static final String[] TASK_DRAFT_PROJECTION = new String[] { MyToDo.TaskDrafts._ID,
      MyToDo.TaskDrafts.COLUMN_NAME_ID, MyToDo.TaskDrafts.COLUMN_NAME_USER_NAME, MyToDo.TaskDrafts.COLUMN_NAME_NAME,
      MyToDo.TaskDrafts.COLUMN_NAME_DESCRIPTION, MyToDo.TaskDrafts.COLUMN_NAME_REMINDER_DATE,
      MyToDo.TaskDrafts.COLUMN_NAME_CREATE_DATE, MyToDo.TaskDrafts.COLUMN_NAME_UPDATE_DATE,
      MyToDo.TaskDrafts.COLUMN_NAME_STATUS };

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void onPerformSync(Account account, Bundle bundle, String authority,
      ContentProviderClient contentProviderClient, SyncResult syncResult) {
    Log.i(TAG, "starting sync");

    String updatedDate = getUpdatedDate(contentProviderClient);
    syncToLocal(account, contentProviderClient, updatedDate);
    syncToServer(account, contentProviderClient);

    Log.i(TAG, "done sync");

  }

  /**
   * @param contentProviderClient
   * @throws RemoteException
   */
  public void syncToLocal(Account account, ContentProviderClient contentProviderClient, String updated) {
    Log.i(TAG, "begin sync to local");
    String urlGetAllTask = URL_HOST + "get-all-task.php";

    // Building Parameters
    String[] keys = new String[] { "username", "updatedDate" };
    String[] values = new String[] { account.name, updated };

    try {
      Log.i(TAG, "Last updatedDate : " + updated);
      JSONObject json = NetworkUtils.postJSONObjFromUrl(urlGetAllTask, keys, values);
      if (json != null) {
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
            contentValues.put(MyToDo.Tasks.COLUMN_NAME_USER_NAME, account.name);
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

            // Kiểm tra tồn tại task?
            Cursor cursor = contentProviderClient.query(MyToDo.Tasks.CONTENT_URI, new String[] { MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE },
                MyToDo.Tasks.COLUMN_NAME_ID + " = ? ", new String[] { jsonObject.getString(MyToDo.Tasks.COLUMN_NAME_ID) }, "_ID DESC LIMIT(1)");

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
      }

    } catch (JSONException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
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
      Cursor cursor = contentProviderClient.query(MyToDo.Tasks.CONTENT_URI,
          new String[] { MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE }, MyToDo.Tasks.COLUMN_NAME_ID + " > ? ",
          new String[] { "0" }, MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE + " DESC LIMIT(1)");
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
    // Building Parameters
    String[] keys = new String[] { "username", "taskId", "name", "description", "reminderDate", "createdDate",
        "updatedDate" };
    boolean noteError;

    try {
      // Uri uri = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_DRAP_URI_BASE, "0");
      Cursor cursor = contentProviderClient.query(MyToDo.TaskDrafts.CONTENT_URI, TASK_DRAFT_PROJECTION, MyToDo.TaskDrafts.COLUMN_NAME_USER_NAME + " = ?", new String[]{account.name},
          null);

      if (cursor.getCount() > 0) {
        Log.i(TAG, "Count cursor : " + cursor.getCount());
        cursor.moveToFirst();
        int colIdIndex = cursor.getColumnIndex(MyToDo.TaskDrafts._ID);
        int colTaskIdIndex = cursor.getColumnIndex(MyToDo.TaskDrafts.COLUMN_NAME_ID);
        int colNameIndex = cursor.getColumnIndex(MyToDo.TaskDrafts.COLUMN_NAME_NAME);
        int colDescriptionIndex = cursor.getColumnIndex(MyToDo.TaskDrafts.COLUMN_NAME_DESCRIPTION);
        int colReminderIndex = cursor.getColumnIndex(MyToDo.TaskDrafts.COLUMN_NAME_REMINDER_DATE);
        int colCreatedDateIndex = cursor.getColumnIndex(MyToDo.TaskDrafts.COLUMN_NAME_CREATE_DATE);
        int colUpdatedDateIndex = cursor.getColumnIndex(MyToDo.TaskDrafts.COLUMN_NAME_UPDATE_DATE);
        int colStatusIndex = cursor.getColumnIndex(MyToDo.TaskDrafts.COLUMN_NAME_STATUS);

        do {
          Long id = cursor.getLong(colIdIndex);
          Long taskId = cursor.getLong(colTaskIdIndex);
          String name = cursor.getString(colNameIndex);
          String description = cursor.getString(colDescriptionIndex);
          String reminderDate = cursor.getString(colReminderIndex);
          String createdDate = cursor.getString(colCreatedDateIndex);
          String updatedDate = cursor.getString(colUpdatedDateIndex);
          Long status = cursor.getLong(colStatusIndex);

          String[] values = new String[] { account.name, taskId.toString(), name, description, reminderDate,
              createdDate, updatedDate };

          switch (status.intValue()) {

          case Constant.TASK_DRAFT_STATUS_INSERT:

            String urlAddTask = URL_HOST + "add-task.php";

            JSONObject addTaskResult = NetworkUtils.postJSONObjFromUrl(urlAddTask, keys, values);
            // check your log for json response
            Log.d("task add result", addTaskResult.toString());

            // json success tag
            noteError = addTaskResult.getBoolean(TAG_ERROR);

            if (!noteError) {
              JSONArray arrTask = addTaskResult.getJSONArray("task");
              for (int i = 0; i < arrTask.length(); i++) {

                ContentValues contentValues = new ContentValues();
                JSONObject jsonObject = arrTask.getJSONObject(i);

                Log.i(TAG, "Start Update task : " + id);
                contentValues.put(MyToDo.Tasks.COLUMN_NAME_ID, jsonObject.getInt(MyToDo.Tasks.COLUMN_NAME_ID));
                // TODO contentValues.put(MyToDo.Tasks.COLUMN_NAME_IS_DRAFT, 1);
                Uri uriUpdateTask = Uri.withAppendedPath(MyToDo.Tasks.CONTENT_ID_URI_BASE, String.valueOf(id));
                contentProviderClient.update(uriUpdateTask, contentValues, null, null);

                Log.i(TAG, "Start delete task draft : " + id);
                Uri uriDeleteTaskDraft = Uri
                    .withAppendedPath(MyToDo.TaskDrafts.CONTENT_ID_URI_BASE, String.valueOf(id));
                contentProviderClient.delete(uriDeleteTaskDraft, null, null);
                Log.i(TAG, "End delete task draft");

                Log.i(TAG, "End Update task");
              }

            } else {
              Log.i(TAG, addTaskResult.getString("message"));
            }
            break;

          case Constant.TASK_DRAFT_STATUS_UPDATE:
            String urlUpdateTask = URL_HOST + "update-task.php";

            JSONObject updateTaskResult = NetworkUtils.postJSONObjFromUrl(urlUpdateTask, keys, values);
            // check your log for json response
            Log.d("task update result", updateTaskResult.toString());

            // json success tag
            noteError = updateTaskResult.getBoolean(TAG_ERROR);

            if (!noteError) {
              JSONArray arrTask = updateTaskResult.getJSONArray("task");
              for (int i = 0; i < arrTask.length(); i++) {
                Log.i(TAG, "Start delete task draft : " + id);
                Uri uriDeleteTaskDraft = Uri
                    .withAppendedPath(MyToDo.TaskDrafts.CONTENT_ID_URI_BASE, String.valueOf(id));
                contentProviderClient.delete(uriDeleteTaskDraft, null, null);
                Log.i(TAG, "End delete task draft");
              }

            } else {
              Log.i(TAG, updateTaskResult.getString("message"));
            }
            break;

          case Constant.TASK_DRAFT_STATUS_DELETE:
            String urlDeleteTask = URL_HOST + "delete-task.php";

            JSONObject deleteTaskResult = NetworkUtils.postJSONObjFromUrl(urlDeleteTask, keys, values);
            // check your log for json response
            Log.d("task delete result", deleteTaskResult.toString());

            // json success tag
            noteError = deleteTaskResult.getBoolean(TAG_ERROR);

            if (!noteError) {
              Log.i(TAG, "Start delete task draft");
              Uri uriDeleteTaskDraft = Uri.withAppendedPath(MyToDo.TaskDrafts.CONTENT_ID_URI_BASE, String.valueOf(id));
              contentProviderClient.delete(uriDeleteTaskDraft, null, null);
              Log.i(TAG, "End delete task draft");
            } else {
              Log.i(TAG, deleteTaskResult.getString("message"));
            }
            break;

          default:
            break;
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

package com.example.mytodo_app.provider;

import java.util.Calendar;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.mytodo_app.utils.CommonUtils;
import com.example.mytodo_app.utils.Constant;

/**
 * Created by Sony on 7/3/2014.
 */
public class MyToDoProvider extends ContentProvider {

  // Used for debugging and logging
  private static final String TAG = "MyToDoProvider";
  /*
   * Constants used by the Uri matcher to choose an action based on the pattern of the incoming URI
   */
  // The incoming URI matches the Tasks URI pattern
  private static final int TASKS = 1;
  // The incoming URI matches the Task ID URI pattern
  private static final int TASK_ID = 2;
  private static final int TASK_SERVER_ID = 3;
  private static final int TASK_DRAFTS = 4;
  private static final int TASK_DRAFT_ID = 5;

  /**
   * A UriMatcher instance
   */
  private static final UriMatcher sUriMatcher;
  /**
   * A projection map used to select columns from the database
   */
  private static HashMap<String, String> sTasksProjectionMap;
  private static HashMap<String, String> sTaskDraftsProjectionMap;

  private static String[] READ_TASK_PROJECTION = { MyToDo.Tasks._ID, MyToDo.Tasks.COLUMN_NAME_ID,
      MyToDo.Tasks.COLUMN_NAME_USER_ID, MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION,
      MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, MyToDo.Tasks.COLUMN_NAME_CREATE_DATE,
      MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE };

  // Handle to a new DatabaseHelper.
  private DatabaseHelper mOpenHelper;

  /**
   * A block that instantiates and sets static objects
   */
  static {

    /*
     * Creates and initializes the URI matcher
     */
    // Create a new instance
    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Add a pattern that routes URIs terminated with "tasks" to a TASKS operation
    sUriMatcher.addURI(MyToDo.AUTHORITY, "tasks", TASKS);

    // Add a pattern that routes URIs terminated with "tasks" plus an integer
    // to a task ID operation
    sUriMatcher.addURI(MyToDo.AUTHORITY, "tasks/#", TASK_ID);
    sUriMatcher.addURI(MyToDo.AUTHORITY, "tasks-server/#", TASK_SERVER_ID);
    sUriMatcher.addURI(MyToDo.AUTHORITY, "task-drafts", TASK_DRAFTS);
    sUriMatcher.addURI(MyToDo.AUTHORITY, "task-drafts/#", TASK_DRAFT_ID);

    /*
     * Creates and initializes a projection map that returns all columns
     */

    // Creates a new projection map instance. The map returns a column name
    // given a string. The two are usually equal.
    sTasksProjectionMap = new HashMap<String, String>();
    sTasksProjectionMap.put(MyToDo.Tasks._ID, MyToDo.Tasks._ID);
    sTasksProjectionMap.put(MyToDo.Tasks.COLUMN_NAME_ID, MyToDo.Tasks.COLUMN_NAME_ID);
    sTasksProjectionMap.put(MyToDo.Tasks.COLUMN_NAME_USER_ID, MyToDo.Tasks.COLUMN_NAME_USER_ID);
    sTasksProjectionMap.put(MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_NAME);
    sTasksProjectionMap.put(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION);
    sTasksProjectionMap.put(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE);
    sTasksProjectionMap.put(MyToDo.Tasks.COLUMN_NAME_CREATE_DATE, MyToDo.Tasks.COLUMN_NAME_CREATE_DATE);
    sTasksProjectionMap.put(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE, MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE);

    sTaskDraftsProjectionMap = new HashMap<String, String>();
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts._ID, MyToDo.TaskDrafts._ID);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_ID, MyToDo.TaskDrafts.COLUMN_NAME_ID);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_USER_ID, MyToDo.TaskDrafts.COLUMN_NAME_USER_ID);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_NAME, MyToDo.TaskDrafts.COLUMN_NAME_NAME);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_DESCRIPTION, MyToDo.TaskDrafts.COLUMN_NAME_DESCRIPTION);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_REMINDER_DATE, MyToDo.TaskDrafts.COLUMN_NAME_REMINDER_DATE);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_CREATE_DATE, MyToDo.TaskDrafts.COLUMN_NAME_CREATE_DATE);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_UPDATE_DATE, MyToDo.TaskDrafts.COLUMN_NAME_UPDATE_DATE);
    sTaskDraftsProjectionMap.put(MyToDo.TaskDrafts.COLUMN_NAME_STATUS, MyToDo.TaskDrafts.COLUMN_NAME_STATUS);
  }

  /**
   * Initializes the provider by creating a new DatabaseHelper. onCreate() is called automatically when Android creates
   * the provider in response to a resolver request from a client.
   */
  @Override
  public boolean onCreate() {
    mOpenHelper = new DatabaseHelper(getContext());
    return true;
  }

  /**
   * This method is called when a client calls
   * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}. Queries the database and
   * returns a cursor containing the results.
   *
   * @return A cursor containing the results of the query. The cursor exists but is empty if the query returns no
   *         results or an exception occurs.
   * @throws IllegalArgumentException
   *           if the incoming URI pattern is invalid.
   */
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

    sortOrder = TextUtils.isEmpty(sortOrder) ? MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE + " DESC" : sortOrder;

    // Constructs a new query builder and sets its table name
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    /**
     * Choose the projection and adjust the "where" clause based on URI pattern-matching.
     */
    switch (sUriMatcher.match(uri)) {
    // If the incoming URI is for notes, chooses the Task projection
    case TASKS:
      qb.setTables(MyToDo.Tasks.TABLE_NAME);
      qb.setProjectionMap(sTasksProjectionMap);
      break;

    /*
     * If the incoming URI is for a single note identified by its ID, chooses the note ID projection, and appends
     * "_ID = <TASK_ID>" to the where clause, so that it selects that single note
     */
    case TASK_ID:
      qb.setTables(MyToDo.Tasks.TABLE_NAME);
      qb.setProjectionMap(sTasksProjectionMap);
      qb.appendWhere(MyToDo.Tasks._ID + // the name of the ID column
          "=" +
          // the position of the note ID itself in the incoming URI
          uri.getPathSegments().get(MyToDo.Tasks.TASK_ID_PATH_POSITION));
      break;
    case TASK_SERVER_ID:
      qb.setTables(MyToDo.Tasks.TABLE_NAME);
      qb.setProjectionMap(sTasksProjectionMap);
      qb.appendWhere(MyToDo.Tasks.COLUMN_NAME_ID + // the name of the ID column
          "=" +
          // the position of the note ID itself in the incoming URI
          uri.getPathSegments().get(MyToDo.Tasks.TASK_ID_PATH_POSITION));
      break;
    case TASK_DRAFTS:
      qb.setTables(MyToDo.TaskDrafts.TABLE_NAME);
      qb.setProjectionMap(sTaskDraftsProjectionMap);
      break;

    /*
     * If the incoming URI is for a single note identified by its ID, chooses the note ID projection, and appends
     * "_ID = <TASK_ID>" to the where clause, so that it selects that single note
     */
    case TASK_DRAFT_ID:
      qb.setTables(MyToDo.TaskDrafts.TABLE_NAME);
      qb.setProjectionMap(sTaskDraftsProjectionMap);
      qb.appendWhere(MyToDo.TaskDrafts._ID + // the name of the ID column
          "=" +
          // the position of the note ID itself in the incoming URI
          uri.getPathSegments().get(MyToDo.TaskDrafts.TASK_DRAFT_ID_PATH_POSITION));
      break;

    default:
      // If the URI doesn't match any of the known patterns, throw an exception.
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    // Opens the database object in "read" mode, since no writes need to be done.
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();

    /*
     * Performs the query. If no problems occur trying to read the database, then a Cursor object is returned;
     * otherwise, the cursor variable contains null. If no records were selected, then the Cursor object is empty, and
     * Cursor.getCount() returns 0.
     */
    Cursor c = qb.query(db, // The database to query
        projection, // The columns to return from the query
        selection, // The columns for the where clause
        selectionArgs, // The values for the where clause
        null, // don't group the rows
        null, // don't filter by row groups
        sortOrder // The sort order
        );

    // Tells the Cursor what URI to watch, so it knows when its source data changes
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  /**
   * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}. Returns the MIME data type
   * of the URI given as a parameter.
   *
   * @param uri
   *          The URI whose MIME type is desired.
   * @return The MIME type of the URI.
   * @throws IllegalArgumentException
   *           if the incoming URI pattern is invalid.
   */
  @Override
  public String getType(Uri uri) {
    /**
     * Chooses the MIME type based on the incoming URI pattern
     */
    switch (sUriMatcher.match(uri)) {
    case TASKS:
      return MyToDo.Tasks.CONTENT_TYPE;
    case TASK_ID:
      return MyToDo.Tasks.CONTENT_ITEM_TYPE;
    case TASK_SERVER_ID:
      return MyToDo.Tasks.CONTENT_ITEM_TYPE;
    case TASK_DRAFTS:
      return MyToDo.TaskDrafts.CONTENT_TYPE;
    case TASK_DRAFT_ID:
      return MyToDo.TaskDrafts.CONTENT_ITEM_TYPE;

      // If the URI pattern doesn't match any permitted patterns, throws an exception.
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    // Validates the incoming URI. Only the full provider URI is allowed for inserts.
    Log.d(TAG, "URI : " + sUriMatcher.match(uri));
    if (sUriMatcher.match(uri) != TASKS) {
      throw new IllegalArgumentException("Unknown URI " + uri);

    }
    // A map to hold the new record's values.
    ContentValues values;

    // If the incoming values map is not null, uses it for the new values.
    if (contentValues != null) {
      values = new ContentValues(contentValues);

    } else {
      // Otherwise, create a new value map
      values = new ContentValues();
    }

    // Opens the database object in "write" database.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    switch (sUriMatcher.match(uri)) {

    case TASKS:

      // If the values map doesn't contain the creation date, sets the value to the current time.
      if (values.containsKey(MyToDo.Tasks.COLUMN_NAME_CREATE_DATE) == false) {
        values.put(MyToDo.Tasks.COLUMN_NAME_CREATE_DATE,
            CommonUtils.getStringDate(Calendar.getInstance(), Constant.DATE_TIME_FORMAT));
      }

      // If the values map doesn't contain the modification date, sets the value to the current
      // time.
      if (values.containsKey(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE) == false) {
        values.put(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE,
            CommonUtils.getStringDate(Calendar.getInstance(), Constant.DATE_TIME_FORMAT));
      }

      // If the values map doesn't contain the modification date, sets the value to the current time.
      if (values.containsKey(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE) == false) {
        values.put(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, "");
      }
      // If the values map doesn't contain a title, sets the value to the default title.
      if (values.containsKey(MyToDo.Tasks.COLUMN_NAME_NAME) == false) {
        Resources r = Resources.getSystem();
        values.put(MyToDo.Tasks.COLUMN_NAME_NAME, r.getString(android.R.string.untitled));
      }

      if (values.containsKey(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION) == false) {
        values.put(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION, "");
      }
      // Performs the insert and returns the ID of the new note.
      long taskId = db.insert(MyToDo.Tasks.TABLE_NAME, // The table to insert into.
          null, // A hack, SQLite sets this column value to null
          // if values is empty.
          values // A map of column names, and the values to insert
          // into the columns.
          );

      // If the insert succeeded, the row ID exists.
      if (taskId > 0) {
        // Insert to task_draft
        Cursor cursor = db.query(MyToDo.Tasks.TABLE_NAME, READ_TASK_PROJECTION, MyToDo.Tasks._ID + " = ?",
            new String[] { String.valueOf(taskId) }, null, null, null, "1");

        ContentValues taskDraftValues;
        if (cursor.moveToFirst() && cursor.getLong(cursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_ID)) == 0) {

          taskDraftValues = new ContentValues();
          DatabaseUtils.cursorRowToContentValues(cursor, taskDraftValues);

          db.insert(MyToDo.TaskDrafts.TABLE_NAME, null, taskDraftValues);
        }

        cursor.close();

        // Creates a URI with the note ID pattern and the new row ID appended to it.
        Uri taskUri = ContentUris.withAppendedId(MyToDo.Tasks.CONTENT_ID_URI_BASE, taskId);

        // Notifies observers registered against this provider that the data changed.
        getContext().getContentResolver().notifyChange(taskUri, null);
        return taskUri;
      }
    }

    // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
    throw new SQLException("Failed to insert row into " + uri);
  }

  /**
   * This is called when a client calls {@link android.content.ContentResolver#delete(Uri, String, String[])}. Deletes
   * records from the database. If the incoming URI matches the note ID URI pattern, this method deletes the one record
   * specified by the ID in the URI. Otherwise, it deletes a a set of records. The record or records must also match the
   * input selection criteria specified by where and whereArgs. If rows were deleted, then listeners are notified of the
   * change.
   *
   * @return If a "where" clause is used, the number of rows affected is returned, otherwise 0 is returned. To delete
   *         all rows and get a row count, use "1" as the where clause.
   * @throws IllegalArgumentException
   *           if the incoming URI pattern is invalid.
   */
  @Override
  public int delete(Uri uri, String where, String[] whereArgs) {
    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    String finalWhere;

    int count;

    // Does the delete based on the incoming URI pattern.
    switch (sUriMatcher.match(uri)) {

    // If the incoming pattern matches the general pattern for notes, does a delete
    // based on the incoming "where" columns and arguments.
    case TASKS:
      count = db.delete(MyToDo.Tasks.TABLE_NAME, // The database table name
          where, // The incoming where clause column names
          whereArgs // The incoming where clause values
          );
      break;

    // If the incoming URI matches a single note ID, does the delete based on the
    // incoming data, but modifies the where clause to restrict it to the
    // particular note ID.
    case TASK_ID:
      /*
       * Starts a final WHERE clause by restricting it to the desired note ID.
       */
      finalWhere = MyToDo.Tasks._ID + // The ID column name
          " = " + // test for equality
          uri.getPathSegments(). // the incoming task ID
              get(MyToDo.Tasks.TASK_ID_PATH_POSITION);

      // If there were additional selection criteria, append them to the final
      // WHERE clause
      if (where != null) {
        finalWhere = finalWhere + " AND " + where;
      }

      // Get dữ liểu để insert vào bảng drafs
      Cursor cursor = db.query(MyToDo.Tasks.TABLE_NAME, READ_TASK_PROJECTION, MyToDo.Tasks._ID + " = ?",
          new String[] { String.valueOf(uri.getPathSegments().get(MyToDo.Tasks.TASK_ID_PATH_POSITION)) }, null, null,
          null, "1");

      Log.i(TAG, "TaskId truoc delete : " + cursor.getCount());
      // Performs the delete.
      count = db.delete(MyToDo.Tasks.TABLE_NAME, // The database table name.
          finalWhere, // The final WHERE clause
          whereArgs // The incoming where clause values.
          );
//      Log.i(TAG, "TaskId sau delete : " + cursor.getInt(cursor.getColumnIndex(MyToDo.Tasks._ID)));
      if (count > 0) {
        // Insert to task_draft

        ContentValues taskDraftValues;
        if (cursor.moveToFirst()) {
          Log.i(TAG, "Insert task draft : " + cursor.getInt(cursor.getColumnIndex(MyToDo.Tasks._ID)));
          taskDraftValues = new ContentValues();
          DatabaseUtils.cursorRowToContentValues(cursor, taskDraftValues);
          taskDraftValues.put(MyToDo.TaskDrafts.COLUMN_NAME_STATUS, Constant.TASK_DRAFT_STATUS_DELETE);
          db.insert(MyToDo.TaskDrafts.TABLE_NAME, null, taskDraftValues);
        }

        cursor.close();
      }

      break;
    case TASK_DRAFTS:
      count = db.delete(MyToDo.TaskDrafts.TABLE_NAME, where, whereArgs);

      break;

    case TASK_DRAFT_ID:
      /*
       * Starts a final WHERE clause by restricting it to the desired note ID.
       */
      finalWhere = MyToDo.TaskDrafts._ID + // The ID column name
          " = " + // test for equality
          uri.getPathSegments(). // the incoming task ID
              get(MyToDo.TaskDrafts.TASK_DRAFT_ID_PATH_POSITION);

      // If there were additional selection criteria, append them to the final
      // WHERE clause
      if (where != null) {
        finalWhere = finalWhere + " AND " + where;
      }
      // Performs the delete.
      count = db.delete(MyToDo.TaskDrafts.TABLE_NAME, // The database table name.
          finalWhere, // The final WHERE clause
          whereArgs // The incoming where clause values.
          );

      break;

    // If the incoming pattern is invalid, throws an exception.
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }
    /*
     * Gets a handle to the content resolver object for the current context, and notifies it that the incoming URI
     * changed. The object passes this along to the resolver framework, and observers that have registered themselves
     * for the provider are notified.
     */
    getContext().getContentResolver().notifyChange(uri, null);

    // Returns the number of rows deleted.
    return count;
  }

  /**
   * This is called when a client calls
   * {@link android.content.ContentResolver#update(Uri, ContentValues, String, String[])} Updates records in the
   * database. The column names specified by the keys in the values map are updated with new data specified by the
   * values in the map. If the incoming URI matches the note ID URI pattern, then the method updates the one record
   * specified by the ID in the URI; otherwise, it updates a set of records. The record or records must match the input
   * selection criteria specified by where and whereArgs. If rows were updated, then listeners are notified of the
   * change.
   *
   * @param uri
   *          The URI pattern to match and update.
   * @param contentValues
   *          A map of column names (keys) and new values (values).
   * @param where
   *          An SQL "WHERE" clause that selects records based on their column values. If this is null, then all records
   *          that match the URI pattern are selected.
   * @param whereArgs
   *          An array of selection criteria. If the "where" param contains value placeholders ("?"), then each
   *          placeholder is replaced by the corresponding element in the array.
   * @return The number of rows updated.
   * @throws IllegalArgumentException
   *           if the incoming URI pattern is invalid.
   */
  @Override
  public int update(Uri uri, ContentValues contentValues, String where, String[] whereArgs) {
    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int count;
    String finalWhere;

    // Does the update based on the incoming URI pattern
    switch (sUriMatcher.match(uri)) {

    // If the incoming URI matches the general notes pattern, does the update based on
    // the incoming data.
    case TASKS:

      // Does the update and returns the number of rows updated.
      count = db.update(MyToDo.Tasks.TABLE_NAME, // The database table name.
          contentValues, // A map of column names and new values to use.
          where, // The where clause column names.
          whereArgs // The where clause column values to select on.
          );
      break;

    // If the incoming URI matches a single note ID, does the update based on the incoming
    // data, but modifies the where clause to restrict it to the particular note ID.
    case TASK_ID:
      /*
       * Starts creating the final WHERE clause by restricting it to the incoming note ID.
       */
      finalWhere = MyToDo.Tasks._ID + // The ID column name
          " = " + // test for equality
          uri.getPathSegments(). // the incoming note ID
              get(MyToDo.Tasks.TASK_ID_PATH_POSITION);

      // If there were additional selection criteria, append them to the final WHERE
      // clause
      if (where != null) {
        finalWhere = finalWhere + " AND " + where;
      }

      // Does the update and returns the number of rows updated.
      count = db.update(MyToDo.Tasks.TABLE_NAME, // The database table name.
          contentValues, // A map of column names and new values to use.
          finalWhere, // The final WHERE clause to use
          // placeholders for whereArgs
          whereArgs // The where clause column values to select on, or
          // null if the values are in the where argument.
          );
      if (count > 0) {

        // Get dữ liểu bảng task để insert vào bảng drafs
        Cursor cursorTask = db.query(MyToDo.Tasks.TABLE_NAME, READ_TASK_PROJECTION, MyToDo.Tasks._ID + " = ?",
            new String[] { String.valueOf(uri.getPathSegments().get(MyToDo.Tasks.TASK_ID_PATH_POSITION)) }, null, null,
            null, "1");

        ContentValues taskDraftValues;
        if (cursorTask.moveToFirst()) {
          Cursor cursorDraft = db.query(MyToDo.TaskDrafts.TABLE_NAME, new String[] { MyToDo.TaskDrafts._ID },
              MyToDo.TaskDrafts._ID + " = ?",
              new String[] { String.valueOf(uri.getPathSegments().get(MyToDo.TaskDrafts.TASK_DRAFT_ID_PATH_POSITION)) }, null,
              null, null, "1");
          // Ton tai thi update
          if (cursorDraft.moveToFirst()) {
            taskDraftValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursorTask, taskDraftValues);
            taskDraftValues.put(MyToDo.TaskDrafts.COLUMN_NAME_STATUS, Constant.TASK_DRAFT_STATUS_UPDATE);

            db.update(MyToDo.TaskDrafts.TABLE_NAME, taskDraftValues, finalWhere, null);
          } else { //Insert
            taskDraftValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursorTask, taskDraftValues);
            taskDraftValues.put(MyToDo.TaskDrafts.COLUMN_NAME_STATUS, Constant.TASK_DRAFT_STATUS_UPDATE);
            db.insert(MyToDo.TaskDrafts.TABLE_NAME, null, taskDraftValues);
          }

        }

        cursorTask.close();
      }
      break;
    // If the incoming pattern is invalid, throws an exception.
    default:
      throw new IllegalArgumentException("Unknown URI " + uri);
    }

    /*
     * Gets a handle to the content resolver object for the current context, and notifies it that the incoming URI
     * changed. The object passes this along to the resolver framework, and observers that have registered themselves
     * for the provider are notified.
     */
    getContext().getContentResolver().notifyChange(uri, null);

    // Returns the number of rows updated.
    return count;
  }
}

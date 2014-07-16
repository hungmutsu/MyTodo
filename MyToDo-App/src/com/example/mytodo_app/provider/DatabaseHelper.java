package com.example.mytodo_app.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "mytodo.db";
  private static final int DATABASE_VERSION = 1;

  private static final String TASK_TABLE_CREATE = String
      .format(
          "CREATE TABLE %s ( %s INTEGER PRIMARY KEY AUTOINCREMENT,%s INTEGER DEFAULT 0,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT,"
          + " %s TEXT)",
          MyToDo.Tasks.TABLE_NAME, MyToDo.Tasks._ID, MyToDo.Tasks.COLUMN_NAME_ID, MyToDo.Tasks.COLUMN_NAME_USER_NAME,
          MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION, MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE,
          MyToDo.Tasks.COLUMN_NAME_CREATE_DATE, MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE);

  private static final String TASK_DRAFT_TABLE_CREATE = String
      .format(
          "CREATE TABLE %s (%s INTEGER PRIMARY KEY,%s INTEGER,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT,"
          + " %s TEXT,%s INTEGER DEFAULT 1)",
          MyToDo.TaskDrafts.TABLE_NAME, MyToDo.TaskDrafts._ID, MyToDo.TaskDrafts.COLUMN_NAME_ID, MyToDo.TaskDrafts.COLUMN_NAME_USER_NAME,
          MyToDo.TaskDrafts.COLUMN_NAME_NAME, MyToDo.TaskDrafts.COLUMN_NAME_DESCRIPTION, MyToDo.TaskDrafts.COLUMN_NAME_REMINDER_DATE,
          MyToDo.TaskDrafts.COLUMN_NAME_CREATE_DATE, MyToDo.TaskDrafts.COLUMN_NAME_UPDATE_DATE, MyToDo.TaskDrafts.COLUMN_NAME_STATUS);
  
  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  /*
   * (non-Javadoc)
   * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
   */
  @Override
  public void onCreate(SQLiteDatabase db) {
    
    db.execSQL(TASK_TABLE_CREATE);
    db.execSQL(TASK_DRAFT_TABLE_CREATE);
  }

  /*
   * (non-Javadoc)
   * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
   */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    db.execSQL("DROP TABLE IF EXISTS " + MyToDo.Tasks.TABLE_NAME);
    db.execSQL("DROP TABLE IF EXISTS " + MyToDo.TaskDrafts.TABLE_NAME);
    onCreate(db);
  }
}

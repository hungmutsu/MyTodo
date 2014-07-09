package com.example.mytodo_app;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.mytodo_app.provider.MyToDo;
import com.example.mytodo_app.reminder.ReminderManager;
import com.example.mytodo_app.utils.CommonUtils;
import com.example.mytodo_app.utils.Constant;

public class TaskEditorActivity extends Activity {

  // For logging and debugging
  private static final String TAG = "TasksListActivity";

  private static final String[] TASK_PROJECTION = new String[] { MyToDo.Tasks._ID, MyToDo.Tasks.COLUMN_NAME_ID,
      MyToDo.Tasks.COLUMN_NAME_USER_ID, MyToDo.Tasks.COLUMN_NAME_NAME, MyToDo.Tasks.COLUMN_NAME_DESCRIPTION,
      MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, MyToDo.Tasks.COLUMN_NAME_CREATE_DATE,
      MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE };

  // This Activity can be started by more than one action. Each action is represented
  // as a "state" constant
  private static final int STATE_EDIT = 0;
  private static final int STATE_INSERT = 1;

  // Global mutable variables
  private int mState;
  private Uri mUri;
  private Cursor mCursor;
  private EditText etName, etDescription, etDate, etTime;

  private Calendar mCalendar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_task_editor);

    /*
     * Creates an Intent to use when the Activity object's result is sent back to the caller.
     */
    final Intent intent = getIntent();

    /*
     * Sets up for the edit, based on the action specified for the incoming Intent.
     */

    // Gets the action that triggered the intent filter for this Activity
    final String action = intent.getAction();

    // For an edit action:
    if (Intent.ACTION_EDIT.equals(action)) {

      // Sets the Activity state to EDIT, and gets the URI for the data to be edited.
      mState = STATE_EDIT;
      mUri = intent.getData();

      // For an insert or paste action:
    } else if (Intent.ACTION_INSERT.equals(action)) {

      // Sets the Activity state to INSERT, gets the general note URI, and inserts an
      // empty record in the provider
      mState = STATE_INSERT;
      mUri = getContentResolver().insert(intent.getData(), null);
      /*
       * If the attempt to insert the new note fails, shuts down this Activity. The originating Activity receives back
       * RESULT_CANCELED if it requested a result. Logs that the insert failed.
       */
      if (mUri == null) {

        // Writes the log identifier, a message, and the URI that failed.
        Log.e(TAG, "Failed to insert new task into " + getIntent().getData());

        // Closes the activity.
        finish();
        return;
      }

      // Since the new entry was created, this sets the result to be returned
      // set the result to be returned.
      setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

      // If the action was other than EDIT or INSERT:
    } else {

      // Logs an error that the action was not understood, finishes the Activity, and
      // returns RESULT_CANCELED to an originating Activity.
      Log.e(TAG, "Unknown action, exiting");
      finish();
      return;
    }

    /*
     * Using the URI passed in with the triggering Intent, gets the note or notes in the provider. Note: This is being
     * done on the UI thread. It will block the thread until the query completes. In a sample app, going against a
     * simple provider based on a local database, the block will be momentary, but in a real app you should use
     * android.content.AsyncQueryHandler or android.os.AsyncTask.
     */
    mCursor = getContentResolver().query(mUri, TASK_PROJECTION, null, null, null);

    // Sets the layout for this Activity. See res/layout/note_editor.xml
    setContentView(R.layout.activity_task_editor);

    // Gets a handle to the EditText in the the layout.
    etName = (EditText) findViewById(R.id.etName);
    etDescription = (EditText) findViewById(R.id.etDescription);
    etDate = (EditText) findViewById(R.id.etDate);
    etTime = (EditText) findViewById(R.id.etTime);
    mCalendar = Calendar.getInstance();
  }

  /**
   * This method is called when the Activity is about to come to the foreground. This happens when the Activity comes to
   * the top of the task stack, OR when it is first starting. Moves to the first note in the list, sets an appropriate
   * title for the action chosen by the user, puts the note contents into the TextView, and saves the original text as a
   * backup.
   */
  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "On Resume");
    /*
     * mCursor is initialized, since onCreate() always precedes onResume for any running process. This tests that it's
     * not null, since it should always contain data.
     */
    if (mCursor != null) {
      // Requery in case something changed while paused (such as the title)
      mCursor = getContentResolver().query(mUri, TASK_PROJECTION, null, null, null);
      mCursor.moveToFirst();

      // Modifies the window title for the Activity according to the current Activity state.
      if (mState == STATE_EDIT) {
        // Set the title of the Activity to include the note title

        int colNameIndex = mCursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_NAME);
        int colReminderIndex = mCursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE);

        String taskName = mCursor.getString(colNameIndex);
        String reminderDate = mCursor.getString(colReminderIndex);
        mCalendar.setTime(CommonUtils.getDate(reminderDate, Constant.DATE_TIME_FORMAT));
        Log.d(TAG, "Calendar time : " + mCalendar.getTime());

        setTitle("Edit : " + taskName);
        etName.setTextKeepState(taskName);
        // Sets the title to "create" for inserts
      } else if (mState == STATE_INSERT) {
        setTitle("New task");
      }

      /*
       * onResume() may have been called after the Activity lost focus (was paused). The user was either editing or
       * creating a note when the Activity paused. The Activity should re-display the text that had been retrieved
       * previously, but it should not move the cursor. This helps the user to continue editing or entering.
       */

      // Gets the note text from the Cursor and puts it in the TextView, but doesn't change
      // the text cursor's position.
      int colDescriptionIndex = mCursor.getColumnIndex(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION);
      String taskDescription = mCursor.getString(colDescriptionIndex);

      etDescription.setTextKeepState(taskDescription);
      etDate.setTextKeepState(CommonUtils.getStringDate(mCalendar, Constant.DATE_FORMAT));
      etTime.setTextKeepState(CommonUtils.getStringDate(mCalendar, Constant.TIME_FORMAT));

      /*
       * Something is wrong. The Cursor should always contain data. Report an error in the note.
       */
    } else {
      /*
       * setTitle("Error"); mText.setText("Error loading task");
       */
    }
  }

  /**
   * This method is called when an Activity loses focus during its normal operation, and is then later on killed. The
   * Activity has a chance to save its state so that the system can restore it. Notice that this method isn't a normal
   * part of the Activity lifecycle. It won't be called if the user simply navigates away from the Activity.
   */

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // TODO Auto-generated method stub
    super.onSaveInstanceState(outState);
  }

  /**
   * This method is called when the Activity loses focus. For Activity objects that edit information, onPause() may be
   * the one place where changes are saved. The Android application model is predicated on the idea that "save" and
   * "exit" aren't required actions. When users navigate away from an Activity, they shouldn't have to go back to it to
   * complete their work. The act of going away should save everything and leave the Activity in a state where Android
   * can destroy it if necessary. If the user hasn't done anything, then this deletes or clears out the note, otherwise
   * it writes the user's work to the provider.
   */
  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "On Pause");
    /*
     * Tests to see that the query operation didn't fail (see onCreate()). The Cursor object will exist, even if no
     * records were returned, unless the query failed because of some exception or error.
     */
    if (mCursor != null) {

      // Get the current note text.
      String name = etName.getText().toString();
      String description = etDescription.getText().toString();
      String reminderDate = CommonUtils.getStringDate(mCalendar, Constant.DATE_TIME_FORMAT);

      if (mState == STATE_EDIT) {
        // Creates a map to contain the new values for the columns
        updateTask(name, description, reminderDate);

        new ReminderManager(this.getBaseContext()).setReminder(
            mCursor.getLong(mCursor.getColumnIndex(MyToDo.Tasks._ID)), mCalendar, name, description);
      } else if (mState == STATE_INSERT) {
        updateTask(name, description, reminderDate);
        new ReminderManager(this.getBaseContext()).setReminder(
            mCursor.getLong(mCursor.getColumnIndex(MyToDo.Tasks._ID)), mCalendar, name, description);
        mState = STATE_EDIT;
      }
    }
  }

  /**
   * This method is called when the user clicks the device's Menu button the first time for this Activity. Android
   * passes in a Menu object that is populated with items. Builds the menus for editing and inserting, and adds in
   * alternative actions that registered themselves to handle the MIME types for this application.
   *
   * @param menu A Menu object to which items should be added.
   * @return True to display the menu.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate menu from XML resource
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.task_editor, menu);

    // Only add extra menu items for a saved task
    if (mState == STATE_EDIT) {
      Intent intent = new Intent(null, mUri);
      intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
      menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, TaskEditorActivity.class), null,
          intent, 0, null);
    }

    return super.onCreateOptionsMenu(menu);
  }

  /**
   * This method is called when a menu item is selected. Android passes in the selected item. The switch statement in
   * this method calls the appropriate method to perform the action the user chose.
   *
   * @param item The selected MenuItem
   * @return True to indicate that the item was processed, and no further work is necessary. False to proceed to further
   *         processing as indicated in the MenuItem object.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle all of the possible menu actions.
    switch (item.getItemId()) {
    case R.id.menu_save:
      String name = etName.getText().toString();
      String description = etDescription.getText().toString();
      String reminderDate = CommonUtils.getStringDate(mCalendar, Constant.DATE_TIME_FORMAT);

      updateTask(name, description, reminderDate);
      finish();
      break;
    case R.id.menu_delete:
      deleteTask();
      finish();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Update task
   * 
   * @param name
   * @param description
   * @param reminderDate
   */
  private final void updateTask(String name, String description, String reminderDate) {

    // Sets up a map to contain values to be updated in the provider.
    ContentValues values = new ContentValues();
    values.put(MyToDo.Tasks.COLUMN_NAME_UPDATE_DATE,
        CommonUtils.getStringDate(Calendar.getInstance(), Constant.DATE_TIME_FORMAT));
    values.put(MyToDo.Tasks.COLUMN_NAME_NAME, name);
    values.put(MyToDo.Tasks.COLUMN_NAME_DESCRIPTION, description);
    values.put(MyToDo.Tasks.COLUMN_NAME_REMINDER_DATE, reminderDate);

    getContentResolver().update(mUri, values, null, null);
  }

  /**
   * Delete task
   */
  private final void deleteTask() {
    if (mCursor != null) {
      mCursor.close();
      mCursor = null;
      getContentResolver().delete(mUri, null, null);
      // TODO : Check lại
      /* mText.setText(""); */
    }
  }

  /**
   * Handle event button SelectDate click
   */
  public void selectDate(View view) {
    DialogFragment newFragment = new SelectDateFragment();
    newFragment.show(getFragmentManager(), "DatePicker");
  }

  /**
   * Handle event button SelectTime click
   */
  public void selectTime(View view) {
    DialogFragment newFragment = new SelectTimeFragment();
    newFragment.show(getFragmentManager(), "TimePicker");
  }

  /**
   * Open date picker
   */
  public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      int yy = mCalendar.get(Calendar.YEAR);
      int mm = mCalendar.get(Calendar.MONTH);
      int dd = mCalendar.get(Calendar.DAY_OF_MONTH);
      return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }

    @Override
    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
      mCalendar.set(yy, mm, dd);
      Log.i(TAG, "Select Date : " + mCalendar.getTime().toString());

      etDate.setText(CommonUtils.getStringDate(mCalendar, Constant.DATE_FORMAT));
    }
  }

  /**
   * Open time picker
   */
  public class SelectTimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
      int minute = mCalendar.get(Calendar.MINUTE);

      return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
      // TODO Auto-generated method stub

      mCalendar.set(Calendar.HOUR_OF_DAY, hour);
      mCalendar.set(Calendar.MINUTE, minute);

      Log.i(TAG, "Select Time : " + mCalendar.getTime().toString());
      etTime.setText(CommonUtils.getStringDate(mCalendar, Constant.TIME_FORMAT));
    }
  }
}

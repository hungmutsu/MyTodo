package com.example.mytodo_app.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sony on 7/3/2014.
 */
public class MyToDo {

  public static final String AUTHORITY = "com.example.mytodolist.provider";

  private MyToDo() {
  }

  public static final class Tasks implements BaseColumns {
    private Tasks() {
    }

    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "tasks";

    /*
     * URI definitions
     */

    /**
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";

    /**
     * Path parts for the URIs
     */

    /**
     * Path part for the Tasks URI
     */
    private static final String PATH_TASKS = "/tasks";
    /**
     * Path part for the Task ID URI
     */
    private static final String PATH_TASK_ID = "/tasks/";

    /**
     * 0-relative position of a task ID segment in the path part of a task ID URI
     */
    public static final int TASK_ID_PATH_POSITION = 1;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TASKS);
    /**
     * The content URI base for a single task. Callers must append a numeric task id to this Uri to retrieve a task
     */
    public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TASK_ID);

    /**
     * The content URI match pattern for a single task, specified by its ID. Use this to match incoming URIs or to
     * construct an Intent.
     */
    public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TASK_ID + "/#");

    /*
     * MIME type definitions
     */

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of tasks.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.example.tasks";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single task.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.tasks";

    /*
     * Column definitions
     */

    /**
     * Column name for the taskId
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String COLUMN_NAME_ID = "taskId";

    /**
     * Column name for the taskId
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String COLUMN_NAME_USER_NAME = "username";

    /**
     * Column name of the task name
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_NAME = "name";

    /**
     * Column name of the description
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_DESCRIPTION = "description";

    /**
     * Column name of the reminderDate
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_REMINDER_DATE = "reminderDate";

    /**
     * Column name of the createdDate
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_CREATE_DATE = "createdDate";

    /**
     * Column name of the updatedDate
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_UPDATE_DATE = "updatedDate";

  }

  public static final class TaskDrafts implements BaseColumns {
    private TaskDrafts() {
    }

    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "task_drafts";

    /*
     * URI definitions
     */

    /**
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";

    /**
     * Path parts for the URIs
     */

    /**
     * Path part for the Tasks URI
     */
    private static final String PATH_TASK_DRAFTS = "/task-drafts";
    /**
     * Path part for the Task ID URI
     */
    private static final String PATH_TASK_DRAFT_ID = "/task-drafts/";

    /**
     * 0-relative position of a task ID segment in the path part of a task ID URI
     */
    public static final int TASK_DRAFT_ID_PATH_POSITION = 1;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TASK_DRAFTS);
    /**
     * The content URI base for a single task. Callers must append a numeric task id to this Uri to retrieve a task
     */
    public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TASK_DRAFT_ID);

    /**
     * The content URI match pattern for a single task, specified by its ID. Use this to match incoming URIs or to
     * construct an Intent.
     */
    public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TASK_DRAFT_ID + "/#");
    /*
     * MIME type definitions
     */

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of tasks.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.example.task_drafts";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single task.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.task_drafts";

    /*
     * Column definitions
     */

    /**
     * Column name for the taskId
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String COLUMN_NAME_ID = "taskId";

    /**
     * Column name for the taskId
     * <P>
     * Type: INTEGER
     * </P>
     */
    public static final String COLUMN_NAME_USER_NAME = "username";

    /**
     * Column name of the task name
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_NAME = "name";

    /**
     * Column name of the description
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_DESCRIPTION = "description";

    /**
     * Column name of the reminderDate
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_REMINDER_DATE = "reminderDate";
    
    /**
     * Column name of the createdDate
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_CREATE_DATE = "createdDate";

    /**
     * Column name of the updatedDate
     * <P>
     * Type: TEXT
     * </P>
     */
    public static final String COLUMN_NAME_UPDATE_DATE = "updatedDate";

    public static final String COLUMN_NAME_STATUS = "status";
  }
}

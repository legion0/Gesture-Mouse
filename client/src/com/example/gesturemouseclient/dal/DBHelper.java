package com.example.gesturemouseclient.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	static final String DATABASE_NAME = "GesureMouseDB";
	static final String GUSTERS_TABLE_NAME = "Gestures";
	static final String GUSTERS_COLUMN_ID = "_id";
	static final String SYSTEM_COLUMN_KEY = "key";
	static final String SYSTEMS_COLUMN_VALUE = "value";
	static final String GUSTERS_COLUMN_NAME = "name";
	static final String GUSTERS_COLUMN_ACTION = "action";
	static final String GUSTERS_COLUMN_MODEL = "model";
	static final String APPLICATIONS_TABLE_NAME = "Applications";
	static final String APPLICATIONS_COLUMN_ID = "_id";
	static final String APPLICATIONS_COLUMN_NAME = "name";
	static final String APPLICATIONS_COLUMN_PROCESS_NAME = "process_name";
	static final String APPLICATIONS_COLUMN_WINDOW_TITLE = "window_title";
	static final String M2M_APPLICATION_GESTURE_TABLE_NAME = "mm_application_gesture";
	static final String SYSTEM_VARIABLES_TABLE_NAME = "system_variables";
	
	static final String M2M_APPLICATION_GESTURE_COLUMN_APP_ID = "aid";
	static final String M2M_APPLICATION_GESTURE_COLUMN_GESTURE_ID = "gid";
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}
	
	public void onCreate(SQLiteDatabase db) {
		String gesturesTable = "create table "+ GUSTERS_TABLE_NAME +" (" +
				GUSTERS_COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
				GUSTERS_COLUMN_NAME +" STRING," +
				GUSTERS_COLUMN_ACTION+ " STRING," +
				GUSTERS_COLUMN_MODEL +" BLOB)";
		db.execSQL(gesturesTable);

		String applicationTable = "create table "+ APPLICATIONS_TABLE_NAME +" (" + APPLICATIONS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
				" " + APPLICATIONS_COLUMN_NAME + " STRING," +
				" " + APPLICATIONS_COLUMN_PROCESS_NAME + " STRING," +
				" " + APPLICATIONS_COLUMN_WINDOW_TITLE + " STRING)";
		db.execSQL(applicationTable);

		String m2mApplicationGestureTable = "create table "+ M2M_APPLICATION_GESTURE_TABLE_NAME +" (" +
				" " + M2M_APPLICATION_GESTURE_COLUMN_GESTURE_ID +" INTEGER," +
				" " + M2M_APPLICATION_GESTURE_COLUMN_APP_ID     +" INTEGER," +
				" UNIQUE (" +
					        M2M_APPLICATION_GESTURE_COLUMN_APP_ID     +
					", " + 	M2M_APPLICATION_GESTURE_COLUMN_GESTURE_ID +
				")" +
			")";
		db.execSQL(m2mApplicationGestureTable);
		
		String systemsVariablesTable = "create table "+ SYSTEM_VARIABLES_TABLE_NAME +" (" + SYSTEM_COLUMN_KEY + " STRING PRIMARY KEY," +
				" " + SYSTEMS_COLUMN_VALUE + " STRING)";
		db.execSQL(systemsVariablesTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
}
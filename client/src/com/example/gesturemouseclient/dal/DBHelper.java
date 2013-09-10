package com.example.gesturemouseclient.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	public static final String GUSTERS_TABLE_NAME = "gestures";
	
	public DBHelper(Context context) {
		super(context, "gesure_db", null, 1);
	}
	
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table "+ GUSTERS_TABLE_NAME +" (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				" name STRING," +
				" action STRING," +
				" model BLOB)";
		db.execSQL(sql );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
}
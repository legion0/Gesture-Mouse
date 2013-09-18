package com.example.gesturemouseclient.dal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wiigee.logic.GestureModel;
import org.wiigee.util.Serializer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * how our gesture lookes like
 * 
 */
public class SystemVariablesDAL {

	public static String get(Context context, String key) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		String table = DBHelper.SYSTEM_VARIABLES_TABLE_NAME;
		String[] columns = {DBHelper.SYSTEM_COLUMN_KEY,DBHelper.SYSTEMS_COLUMN_VALUE}; 
		String selection = DBHelper.SYSTEM_COLUMN_KEY+ " = ?";
		String[] selectionArgs = {key}; 
		String groupBy = null;
		String having = null;
		String orderBy = null;

		Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having,	orderBy);

		String dbKey = null;
		String value = null;

		if (cursor.moveToFirst()) {
			do {
				dbKey = cursor.getString(0);
				value = cursor.getString(1);
				Log.d("SystemVariableDal","key: "+dbKey+" ,value: "+value);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return value;
	}

	public static void set(Context context,String key,String value) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBHelper.SYSTEM_COLUMN_KEY, key);
		values.put(DBHelper.SYSTEMS_COLUMN_VALUE, value);
		
		db.insertWithOnConflict(DBHelper.SYSTEM_VARIABLES_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		db.close();
	}


	

}

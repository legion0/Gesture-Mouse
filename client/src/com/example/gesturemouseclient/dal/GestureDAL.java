package com.example.gesturemouseclient.dal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.wiigee.logic.GestureModel;
import org.wiigee.util.Serializer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * how our gesture lookes like
 * 
 */
public class GestureDAL {

	private int id;
	private String name;
	private List<Integer> action;
	private GestureModel model;

	public GestureDAL(int id, String name, List<Integer> action,
			GestureModel model) {
		super();
		this.id = id;
		this.name = name;
		this.action = action;
		this.model = model;
	}

	public static List<GestureDAL> load(Context context) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		
//		sCursor cursor = db.query(DBHelper.GUSTERS_TABLE_NAME, new String[]);
//				Cursor cursor =
//						db.query(“students”, new String[] { “tz”, “name” },
//								“name like ‘?%’”, new String[] { prefix },
//										null, null, “tz desc”);
//				if (cursor.moveToFirst()) {
//					do {
//						String name = cursor.getString(1); //...and so on
//					} while (cursor.moveToNext());
//				}
		
		
		
		return null;
	}

	public void save(Context context) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		try {
			Serializer.write(model, outputStream);
			byte[] buffer = new byte[outputStream.size()];
			outputStream.write(buffer);

			ContentValues gestureValues = new ContentValues();
			gestureValues.put("name", name);
			gestureValues.put("action", action.toString());
			gestureValues.put("model", buffer);
			db.insert("gestures", null, gestureValues);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getAction() {
		return action;
	}

	public void setAction(List<Integer> action) {
		this.action = action;
	}

	public GestureModel getModel() {
		return model;
	}

	public void setModel(GestureModel model) {
		this.model = model;
	}

}

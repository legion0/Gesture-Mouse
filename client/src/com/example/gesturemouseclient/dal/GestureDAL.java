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

/**
 * how our gesture lookes like
 * 
 */
public class GestureDAL {

	private Integer id;
	private String name;
	private List<Integer> action;
	private GestureModel model;

	public GestureDAL(String name, List<Integer> action,
			GestureModel model) {
		super();
		this.name = name;
		this.action = action;
		this.model = model;
	}

	public static Set<GestureDAL> load(Context context, int appId) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		//DBHelper.GUSTERS_TABLE_NAME, new String[] {"id","name","action","model"},null,null);



		int[] gestureIdArr = getGestureIds(context,appId);
		String gestureIdStringArr = Arrays.toString(gestureIdArr);
		gestureIdStringArr = "("+gestureIdStringArr.substring(1,gestureIdStringArr.length()-1)+")";

		String table = DBHelper.GUSTERS_TABLE_NAME;
		String[] columns = {DBHelper.GUSTERS_COLUMN_ID,DBHelper.GUSTERS_COLUMN_NAME,DBHelper.GUSTERS_COLUMN_ACTION,DBHelper.GUSTERS_COLUMN_MODEL}; 
		String selection = DBHelper.GUSTERS_COLUMN_ID+ " IN "+gestureIdStringArr;
		String[] selectionArgs = null; 
		String groupBy = null;
		String having = null;
		String orderBy = null;



		Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having,	orderBy);

		Set<GestureDAL> gestureSet = new HashSet<GestureDAL>();

		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(0);
				String name = cursor.getString(1);
				List<Integer> action = StringToIntegerList(cursor.getString(2));
				InputStream stream = new ByteArrayInputStream(cursor.getBlob(3));
				GestureModel model = null;
				try {
					model = Serializer.read(stream );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				GestureDAL g = new GestureDAL(name, action, model);
				g.id = id;
				gestureSet.add(g);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return gestureSet;
	}

	private static List<Integer> StringToIntegerList(String str) {
		str = str.substring(1,str.length()-1);

		String[] strArr = str.split(", ");
		List<Integer> output = new ArrayList<Integer>();

		for (int i = 0; i < strArr.length; i++) {
			output.add(Integer.parseInt(strArr[i]));
		}
		return output;
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
			if (id != null) {
				gestureValues.put(DBHelper.APPLICATIONS_COLUMN_ID, id);
			}
			gestureValues.put(DBHelper.GUSTERS_COLUMN_NAME, name);
			gestureValues.put(DBHelper.GUSTERS_COLUMN_ACTION, action.toString());
			gestureValues.put(DBHelper.GUSTERS_COLUMN_MODEL, buffer);
			long newId = db.insertWithOnConflict(DBHelper.GUSTERS_TABLE_NAME, null, gestureValues,
					SQLiteDatabase.CONFLICT_REPLACE);
			if(newId == -1)
			{
				//TODO: handle error
				db.close();
				return;
			}
			id = (int) newId;
			db.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Integer getId() {
		return id;
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

	static int[] getGestureIds(Context context, int appId) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(
				DBHelper.M2M_APPLICATION_GESTURE_TABLE_NAME,
				new String[] {DBHelper.M2M_APPLICATION_GESTURE_COLUMN_GESTURE_ID},
				String.format("%s = ?", DBHelper.M2M_APPLICATION_GESTURE_COLUMN_APP_ID),
				new String[] {Integer.toString(appId)},
				null, null, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			db.close();
			return new int[0];
		}
		int[] gids = new int[cursor.getCount()];
		int i = 0;
		do {
			gids[i++] = cursor.getInt(0);
		} while (cursor.moveToNext());
		cursor.close();
		db.close();
		return gids;
	}

	public void addToApplication(Context context, int appId) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBHelper.M2M_APPLICATION_GESTURE_COLUMN_GESTURE_ID, id);
		values.put(DBHelper.M2M_APPLICATION_GESTURE_COLUMN_APP_ID, appId);
		db.insertWithOnConflict(DBHelper.M2M_APPLICATION_GESTURE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		db.close();
	}

}

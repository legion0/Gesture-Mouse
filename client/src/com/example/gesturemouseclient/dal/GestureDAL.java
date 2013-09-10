package com.example.gesturemouseclient.dal;

import java.io.BufferedInputStream;
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

	public static Set<GestureDAL> load(Context context, int appId) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();

		//DBHelper.GUSTERS_TABLE_NAME, new String[] {"id","name","action","model"},null,null);



		int[] gestureIdArr = getGestureIds(context,appId);
		String gestureIdStringArr = Arrays.toString(gestureIdArr);
		gestureIdStringArr = "("+gestureIdStringArr.substring(1,gestureIdStringArr.length()-1)+")";

		String table = DBHelper.GUSTERS_TABLE_NAME;
		String[] columns = {"id","name","action","model"}; 
		String selection = "_id IN "+gestureIdStringArr;
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
				GestureModel model = Serializer.read(stream ); 
				GestureDAL g = new GestureDAL(id, name, action, model);
				gestureSet.add(g);
			} while (cursor.moveToNext());
		}
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
			gestureValues.put("name", name);
			gestureValues.put("action", action.toString());
			gestureValues.put("model", buffer);
			db.insertWithOnConflict("gestures", null, gestureValues,
					SQLiteDatabase.CONFLICT_IGNORE);

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

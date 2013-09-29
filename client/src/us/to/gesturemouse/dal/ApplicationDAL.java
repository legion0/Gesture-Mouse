package us.to.gesturemouse.dal;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ApplicationDAL {

	private static final String[] APPLICATIONS_COLUMNS = { DBHelper.APPLICATIONS_COLUMN_ID, DBHelper.APPLICATIONS_COLUMN_NAME,
			DBHelper.APPLICATIONS_COLUMN_PROCESS_NAME, DBHelper.APPLICATIONS_COLUMN_WINDOW_TITLE };

	private Integer id = null;

	private String name;
	private String processName;
	private String windowTitle;
	private Set<GestureDAL> gestures;

	public ApplicationDAL(String name, String processName, String windowTitle) {
		super();
		this.name = name;
		this.processName = processName;
		this.windowTitle = windowTitle;
		gestures = new HashSet<GestureDAL>();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getWindowTitle() {
		return windowTitle;
	}

	public void setWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	public void save(Context context) {
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		if (id != null) {
			values.put(DBHelper.APPLICATIONS_COLUMN_ID, id);
		}
		values.put(DBHelper.APPLICATIONS_COLUMN_NAME, name);
		values.put(DBHelper.APPLICATIONS_COLUMN_PROCESS_NAME, processName);
		values.put(DBHelper.APPLICATIONS_COLUMN_WINDOW_TITLE, windowTitle);
		long newId = db.insertWithOnConflict(DBHelper.APPLICATIONS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (newId == -1) {
			// TODO: handle error
			return;
		}
		id = (int) newId;
		db.close();
	}

	public Set<GestureDAL> getGestures() {
		return gestures;
	}

	public void loadGestures(Context context) {
		this.gestures = GestureDAL.load(context, id);
	}

	public static Set<ApplicationDAL> load(Context context) {
		Set<ApplicationDAL> apps = new HashSet<ApplicationDAL>();
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(DBHelper.APPLICATIONS_TABLE_NAME, APPLICATIONS_COLUMNS, null, null, null, null, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			db.close();
			return apps;
		}
		do {
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			String processName = cursor.getString(2);
			String windowTitle = cursor.getString(3);
			ApplicationDAL app = new ApplicationDAL(name, processName, windowTitle);
			app.id = id;
			apps.add(app);
		} while (cursor.moveToNext());
		cursor.close();
		db.close();
		return apps;
	}

	public static Set<ApplicationDAL> loadWithGestures(Context context) {
		Set<ApplicationDAL> apps = load(context);
		for (ApplicationDAL app : apps) {
			app.loadGestures(context);
		}
		return apps;
	}
}

package software.univalle.srcg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBController extends SQLiteOpenHelper {

	public DBController(Context applicationcontext) {
		super(applicationcontext, "srcg.db", null, 1);
	}
	//Creates Table
	@Override
	public void onCreate(SQLiteDatabase database) {
		String query;
		query = "CREATE TABLE graduandos ( codigo INTEGER,programa TEXT, nombre TEXT, apellido TEXT," +
				"PRIMARY KEY (codigo))";
		database.execSQL(query);
		query = "CREATE TABLE invitados ( documento INTEGER, graduando INTEGER, nombre TEXT, apellido TEXT,asistencia INTEGER," +
				"PRIMARY KEY (documento)," +
				"FOREIGN KEY (graduando) REFERENCES graduandos(codigo))";
		database.execSQL(query);
	}
	@Override
	public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
		String query;
		query = "DROP TABLE IF EXISTS invitados";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS graduandos";
		database.execSQL(query);
		onCreate(database);
	}

	public void update(){
		SQLiteDatabase database = this.getWritableDatabase();
		String query;
		query = "DROP TABLE IF EXISTS invitados";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS graduandos";
		database.execSQL(query);
		onCreate(database);
	}

	/**
	 * Inserts User into SQLite DB
	 * @param queryValues
	 */
	public void insertGuest(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("documento", queryValues.get("documento"));
		values.put("graduando", queryValues.get("graduando"));
		values.put("nombre", queryValues.get("nombre"));
		values.put("apellido", queryValues.get("apellido"));
		values.put("asistencia", queryValues.get("asistencia"));
		database.insert("invitados", null, values);
		database.close();
	}

	/**
	 * Inserts User into SQLite DB
	 * @param queryValues
	 */
	public void insertGraduate(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("codigo", queryValues.get("codigo"));
		values.put("nombre", queryValues.get("nombre"));
		values.put("apellido", queryValues.get("apellido"));
		values.put("programa", queryValues.get("programa"));
		database.insert("graduandos", null, values);
		database.close();
	}

	/**
	 * Get list of Users from SQLite DB as Array List
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getAllGuests() {
		ArrayList<HashMap<String, String>> guestsList;
		guestsList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT  * FROM invitados ORDER BY apellido";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("documento", cursor.getString(0));
				map.put("graduando", cursor.getString(1));
				map.put("nombre", cursor.getString(2));
				map.put("apellido", cursor.getString(3));
				map.put("asistencia", cursor.getString(4));
				guestsList.add(map);
			} while (cursor.moveToNext());
		}
		database.close();
		return guestsList;
	}

	/**
	 * Get list of Users from SQLite DB as Array List
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getAllGraduates() {
		ArrayList<HashMap<String, String>> graduatesList;
		graduatesList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT  * FROM graduandos ORDER BY apellido";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("codigo", cursor.getString(0));
				map.put("programa", cursor.getString(1));
				map.put("nombre", cursor.getString(2));
				map.put("apellido", cursor.getString(3));
				graduatesList.add(map);
			} while (cursor.moveToNext());
		}
		database.close();
		return graduatesList;
	}

	/**
	 * Get list of Users from SQLite DB as Array List
	 * @return
	 */
	public ArrayList<HashMap<String, String>> searchGuests(String codigo) {
		ArrayList<HashMap<String, String>> guestsList;
		guestsList = new ArrayList<HashMap<String, String>>();
		String selectQuery = "SELECT * FROM invitados WHERE graduando = '"+codigo+"' ORDER BY apellido";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("documento", cursor.getString(0));
				map.put("graduando", cursor.getString(1));
				map.put("nombre", cursor.getString(2));
				map.put("apellido", cursor.getString(3));
				map.put("asistencia", cursor.getString(4));
				guestsList.add(map);
			} while (cursor.moveToNext());
		}
		database.close();
		return guestsList;
	}

}
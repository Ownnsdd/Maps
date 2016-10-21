package com.xb.maps;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBconnection {
	public static final String DATABASE_NAME = "friend.db";
	public static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE = "CREATE TABLE if not exists friend("
			+ "_id integer primary key autoincrement," + "name text," + "phone text)";

	public static SQLiteDatabase open(Context context) {
		return new DBOpenHelper(context).getWritableDatabase();
	}

	public static void close(SQLiteDatabase db) {
		if (db != null)
			db.close();
	}

	private static class DBOpenHelper extends SQLiteOpenHelper {

		public DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS friend");
			onCreate(db);
		}

	}
}

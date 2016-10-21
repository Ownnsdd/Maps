package com.xb.maps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class dbTools {
	private SQLiteDatabase db;

	public dbTools(SQLiteDatabase database) {
		this.db = database;
	}

	public void insert(Friend friend) {
		ContentValues values = new ContentValues();
		values.put("name", friend.getName());
		values.put("phone", friend.getPhone());
		db.insert("friend", null, values);
	}

	public void delete(long id) {
		db.execSQL("delete frome friend where _id=" + id);
		db.execSQL("update friend set _id=_id-1 where _id > ?" + id);
	}

	public void update(Friend friend) {
		db.execSQL("update friend set name=?,phone=? where _id=?",
				new Object[] { friend.getName(), friend.getPhone(), friend.getID() });
	}

	public Friend findByPhone(String phone) {
		Friend friend = new Friend();
		Cursor cursor = db.query("friend", null, "phone="+phone, null, null, null, null);
		if (cursor.moveToNext()) {
			friend.setID(cursor.getLong(0));
			friend.setName(cursor.getString(cursor.getColumnIndex("name")));
			friend.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
		}
		cursor.close();
		return friend;
	}
	
	public Friend findById(long id) {
		Friend friend = new Friend();
		Cursor cursor = db.query("friend", null, "_id="+id, null, null, null, null);
		if (cursor.moveToNext()) {
			friend.setID(cursor.getLong(0));
			friend.setName(cursor.getString(cursor.getColumnIndex("name")));
			friend.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
		}
		cursor.close();
		return friend;
	}

	public Cursor findAll() {
		return db.rawQuery("select _id, name from friend order by _id", null);
	}
}

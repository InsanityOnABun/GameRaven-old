package com.ioabsoftware.DroidFAQs.db;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighlightListDBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "highlightlists.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_USERS = "users";
	public static final String COLUMN_USERS_ID = "_id";
	public static final String COLUMN_USERS_NAME = "name";
	public static final String COLUMN_USERS_LABEL = "label";
	public static final String COLUMN_USERS_COLOR = "color";
	
	private static final String CREATE_TABLE_USERS = 
			"create table " + TABLE_USERS + "(" + 
			COLUMN_USERS_ID + " integer primary key autoincrement " +
			COLUMN_USERS_NAME + " text primary key, " + 
			COLUMN_USERS_LABEL + " text not null, " +
			COLUMN_USERS_COLOR + " integer not null);";

	public HighlightListDBHelper(Context context) {
	  super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_USERS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		// TODO Auto-generated method stub

	}
	
	public void addUser(String name, String label, int color) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues vals = new ContentValues();
		vals.put(COLUMN_USERS_NAME, name);
		vals.put(COLUMN_USERS_LABEL, label);
		vals.put(COLUMN_USERS_COLOR, color);
		
		db.insert(TABLE_USERS, null, vals);
		db.close();
	}
	
	public void updateUser(String name, String label, int color) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues vals = new ContentValues();
		vals.put(COLUMN_USERS_NAME, name);
		vals.put(COLUMN_USERS_LABEL, label);
		vals.put(COLUMN_USERS_COLOR, color);
		
		db.update(TABLE_USERS, vals, COLUMN_USERS_NAME + " = " + name, null);
		db.close();
	}
	
	public void deleteUser(String name) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_USERS, COLUMN_USERS_NAME + " = " + name, null);
		db.close();
	}
	
//	public HashMap<String, String> getAllUsers() {
//		SQLiteDatabase db = getWritableDatabase();
//		HashMap<String, String> users = new HashMap<String, String>();
//		
//		Cursor cur = db.query(TABLE_USERS, null, null, null, null, null, null);
//		
//		if (cur.moveToFirst()) {
//			do {
//				users.put(cur.getString(0), cur.getString(1));
//			} while (cur.moveToNext());
//		}
//		
//		return users;
//	}

}

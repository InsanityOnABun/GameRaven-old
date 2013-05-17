package com.ioabsoftware.DroidFAQs.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HighlightListDBHelper extends SQLiteOpenHelper {
	
	private HashMap<String, HighlightedUser> highlightedUsers;
	/**
	 * DO NOT STORE REFERENCES TO THIS LIST! Always call this method when needing to use the list,
	 * this list will always be up to date.
	 * @return An up-to-date list of highlighted users.
	 */
	public HashMap<String, HighlightedUser> getHighlightedUsers() {return highlightedUsers;}
	
	private static final String DATABASE_NAME = "highlightlists.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_USERS = "users";
	public static final String COLUMN_USERS_ID = "_id";
	public static final String COLUMN_USERS_NAME = "name";
	public static final String COLUMN_USERS_LABEL = "label";
	public static final String COLUMN_USERS_COLOR = "color";
	
	private static final String CREATE_TABLE_USERS = 
			"create table " + TABLE_USERS + "(" + 
			COLUMN_USERS_ID + " integer primary key autoincrement, " +
			COLUMN_USERS_NAME + " text not null, " + 
			COLUMN_USERS_LABEL + " text not null, " +
			COLUMN_USERS_COLOR + " integer not null);";

	public HighlightListDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		SQLiteDatabase db = getWritableDatabase();
		updateUsers(db);
		db.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_USERS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		// TODO Auto-generated method stub

	}
	
	public HashMap<String, HighlightedUser> addUser(String name, String label, int color) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues vals = new ContentValues();
		vals.put(COLUMN_USERS_NAME, name);
		vals.put(COLUMN_USERS_LABEL, label);
		vals.put(COLUMN_USERS_COLOR, color);
		
		db.insert(TABLE_USERS, null, vals);
		
		updateUsers(db);
		
		db.close();
		
		return highlightedUsers;
	}
	
	public HashMap<String, HighlightedUser> updateUser(HighlightedUser user) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues vals = new ContentValues();
		vals.put(COLUMN_USERS_NAME, user.getName());
		vals.put(COLUMN_USERS_LABEL, user.getLabel());
		vals.put(COLUMN_USERS_COLOR, user.getColor());
		
		db.update(TABLE_USERS, vals, COLUMN_USERS_ID + " = " + user.getID(), null);
		
		updateUsers(db);
		
		db.close();
		
		return highlightedUsers;
	}
	
	public HashMap<String, HighlightedUser> deleteUser(HighlightedUser user) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TABLE_USERS, COLUMN_USERS_ID + " = " + user.getID(), null);
		
		updateUsers(db);
		
		db.close();
		
		return highlightedUsers;
	}
	
	private void updateUsers(SQLiteDatabase db) {
		highlightedUsers = new HashMap<String, HighlightedUser>();
		
		Cursor cur = db.query(TABLE_USERS, null, null, null, null, null, null);
		
		if (cur.moveToFirst()) {
			do {
				highlightedUsers.put(cur.getString(1).toLowerCase(Locale.US), 
						new HighlightedUser(cur.getInt(0), cur.getString(1), cur.getString(2), cur.getInt(3)));
			} while (cur.moveToNext());
		}
	}

}

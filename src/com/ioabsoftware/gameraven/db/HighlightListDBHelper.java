package com.ioabsoftware.gameraven.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Toast;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app._HoloActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

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
	
	/**
	 * Shows a dialog used to add or update user highlighting.
	 * @param c Activity used as a Context and for getLayoutInflator
	 * @param user HighlightedUser object. If null or ID equals -1, this is a new user being highlighted.
	 * @param username Optional username to set. Does nothing if this isn't a new user being highlighted.
	 * @param listener Optional listener to be fired just before the dialog gets dismissed on successful save.
	 */
	public static void showHighlightUserDialog(final Activity c, final HighlightedUser user, 
											   String username, final HlUDDismissListener listener) {
		boolean isNewCheck = false;
		if (user == null || user.getID() == -1)
			isNewCheck = true;
		
		final boolean isAddNew = isNewCheck;
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(c);
    	LayoutInflater inflater = ((_HoloActivity) c).getLayoutInflater();
    	final View dialogView = inflater.inflate(R.layout.highlightuserdialog, null);
    	dialogBuilder.setView(dialogView);
    	dialogBuilder.setTitle("Add Highlighted User");
    	
    	final EditText dName = (EditText) dialogView.findViewById(R.id.huName);
    	final EditText dLabel = (EditText) dialogView.findViewById(R.id.huLabel);

    	final Button dSetColor = (Button) dialogView.findViewById(R.id.huSetColor);
    	final TextView dColorVal = (TextView) dialogView.findViewById(R.id.huColorVal);
    	
    	final CheckedTextView dDelete = (CheckedTextView) dialogView.findViewById(R.id.huDelete);
    	
    	dSetColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int startColor = Integer.parseInt(dColorVal.getText().toString());
				
				ColorPickerDialog picker = new ColorPickerDialog(c, startColor);
				picker.setOnColorChangedListener(new OnColorChangedListener() {
					@Override
					public void onColorChanged(int color) {
						dSetColor.setBackgroundColor(color);
						dSetColor.setTextColor(~color | 0xFF000000); //without alpha
						dColorVal.setText(Integer.toString(color));
					}
				});
				picker.setHexValueEnabled(true);
				picker.show();
			}
		});
    	
    	if (!isAddNew) {
    		dDelete.setVisibility(View.VISIBLE);
    		dDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((CheckedTextView) v).toggle();
				}
			});
    		
    		dName.setText(user.getName());
    		dLabel.setText(user.getLabel());
    		dSetColor.setBackgroundColor(user.getColor());
			dSetColor.setTextColor(~user.getColor() | 0xFF000000); //without alpha
    		dColorVal.setText(Integer.toString(user.getColor()));
    	}
    	else if (username != null)
    		dName.setText(username);
    	
    	dialogBuilder.setPositiveButton("Save", null);
    	dialogBuilder.setNegativeButton("Cancel", null);
    	
    	
    	final AlertDialog diag = dialogBuilder.create();
    	diag.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				diag.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean shouldDismiss = true;
						
						if (!dDelete.isChecked()) {
							if (dName.getText().toString().length() > 0
									&& dLabel.getText().toString().length() > 0
									&& !dColorVal.getText().equals("0")) {

								if (isAddNew) {
									AllInOneV2.getHLDB().addUser(
											dName.getText().toString(),
											dLabel.getText().toString(),
											Integer.parseInt(dColorVal
													.getText().toString()));
								} else {
									user.setName(dName.getText().toString());
									user.setLabel(dLabel.getText().toString());
									user.setColor(Integer.parseInt(dColorVal
											.getText().toString()));
									AllInOneV2.getHLDB().updateUser(user);
								}

								
							} else {
								Toast.makeText(c,
										"Missing required info",
										Toast.LENGTH_SHORT).show();
								shouldDismiss = false;
							}
						}
						else {
							AllInOneV2.getHLDB().deleteUser(user);
						}
						
						if (shouldDismiss) {
							if (listener != null)
								listener.beforeDismissSuccessfulSave();
							
							diag.dismiss();
						}
					}
				});
			}
		});
    	diag.show();
	}

}

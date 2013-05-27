package com.ioabsoftware.gameraven.views;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.SettingsHighlightedUsers;
import com.ioabsoftware.gameraven.db.HighlightListDBHelper;
import com.ioabsoftware.gameraven.db.HighlightedUser;

public class HighlightedUserView extends LinearLayout implements OnClickListener {
	
	private SettingsHighlightedUsers hlActivity;
	
	private HighlightedUser user;
	
	private TextView nameView, labelView;
	private LinearLayout colorFrame;
	
	private boolean isAddNew = false;

	public HighlightedUserView(SettingsHighlightedUsers hlActivityIn, HighlightedUser userIn) {
		super(hlActivityIn);
		hlActivity = hlActivityIn;
		
		LayoutInflater inflater = (LayoutInflater) hlActivityIn.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.highlighteduserview, this);
        
        user = userIn;
        
        nameView = (TextView) findViewById(R.id.hvName);
        labelView = (TextView) findViewById(R.id.hvLabel);
        colorFrame = (LinearLayout) findViewById(R.id.hvColorFrame);
        
        nameView.setText(user.getName());
        labelView.setText(user.getLabel());
        
        if (user.getColor() != 0)
        	colorFrame.setBackgroundColor(user.getColor());
        
        if (user.getID() == -1)
        	isAddNew = true;
        
        setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		HighlightListDBHelper.showHighlightUserDialog(hlActivity, user, null, hlActivity);
//		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(hlActivity);
//    	LayoutInflater inflater = hlActivity.getLayoutInflater();
//    	final View dialogView = inflater.inflate(R.layout.highlightuserdialog, null);
//    	dialogBuilder.setView(dialogView);
//    	dialogBuilder.setTitle("Add Highlighted User");
//    	
//    	final EditText dName = (EditText) dialogView.findViewById(R.id.huName);
//    	final EditText dLabel = (EditText) dialogView.findViewById(R.id.huLabel);
//
//    	final Button dSetColor = (Button) dialogView.findViewById(R.id.huSetColor);
//    	final TextView dColorVal = (TextView) dialogView.findViewById(R.id.huColorVal);
//    	
//    	final CheckedTextView dDelete = (CheckedTextView) dialogView.findViewById(R.id.huDelete);
//    	
//    	dSetColor.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				int startColor = Integer.parseInt(dColorVal.getText().toString());
//				
//				ColorPickerDialog picker = new ColorPickerDialog(hlActivity, startColor);
//				picker.setOnColorChangedListener(new OnColorChangedListener() {
//					@Override
//					public void onColorChanged(int color) {
//						dSetColor.setBackgroundColor(color);
//						dSetColor.setTextColor(~color | 0xFF000000); //without alpha
//						dColorVal.setText(Integer.toString(color));
//					}
//				});
//				picker.show();
//			}
//		});
//    	
//    	if (!isAddNew) {
//    		dDelete.setVisibility(View.VISIBLE);
//    		dDelete.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					((CheckedTextView) v).toggle();
//				}
//			});
//    		
//    		dName.setText(user.getName());
//    		dLabel.setText(user.getLabel());
//    		dSetColor.setBackgroundColor(user.getColor());
//			dSetColor.setTextColor(~user.getColor() | 0xFF000000); //without alpha
//    		dColorVal.setText(Integer.toString(user.getColor()));
//    	}
//    	
//    	dialogBuilder.setPositiveButton("Save", null);
//    	dialogBuilder.setNegativeButton("Cancel", null);
//    	
//    	
//    	final AlertDialog diag = dialogBuilder.create();
//    	diag.setOnShowListener(new OnShowListener() {
//			@Override
//			public void onShow(DialogInterface dialog) {
//				diag.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						boolean shouldDismiss = true;
//						
//						if (!dDelete.isChecked()) {
//							if (dName.getText().toString().length() > 0
//									&& dLabel.getText().toString().length() > 0
//									&& !dColorVal.getText().equals("0")) {
//
//								if (isAddNew) {
//									AllInOneV2.getHLDB().addUser(
//											dName.getText().toString(),
//											dLabel.getText().toString(),
//											Integer.parseInt(dColorVal
//													.getText().toString()));
//								} else {
//									user.setName(dName.getText().toString());
//									user.setLabel(dLabel.getText().toString());
//									user.setColor(Integer.parseInt(dColorVal
//											.getText().toString()));
//									AllInOneV2.getHLDB().updateUser(user);
//								}
//
//								
//							} else {
//								Toast.makeText(hlActivity,
//										"Missing required info",
//										Toast.LENGTH_SHORT).show();
//								shouldDismiss = false;
//							}
//						}
//						else {
//							AllInOneV2.getHLDB().deleteUser(user);
//						}
//						
//						if (shouldDismiss) {
//							hlActivity.updateList();
//							diag.dismiss();
//						}
//					}
//				});
//			}
//		});
//    	diag.show();
	}
	
	

}

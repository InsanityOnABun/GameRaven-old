package com.ioabsoftware.DroidFAQs;

import java.util.Map.Entry;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.Toast;

import com.ioabsoftware.DroidFAQs.Views.HeaderView;
import com.ioabsoftware.DroidFAQs.db.HighlightListDBHelper;
import com.ioabsoftware.DroidFAQs.db.HighlightedUser;
import com.ioabsoftware.gameraven.R;

import de.devmil.common.ui.color.ColorSelectorDialog;
import de.devmil.common.ui.color.ColorSelectorDialog.OnColorChangedListener;

import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsHighlightedUsers extends Activity {

	private LinearLayout wrapper;
	private Button addUser;
	private HighlightListDBHelper hlDB;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		wrapper = new LinearLayout(this);
		addUser = new Button(this);
		
		hlDB = new HighlightListDBHelper(this);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		addUser.setLayoutParams(blp);
		addUser.setText("Add New Highlighted User");
		addUser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder b = new AlertDialog.Builder(SettingsHighlightedUsers.this);
		    	LayoutInflater inflater = getLayoutInflater();
		    	final View d = inflater.inflate(R.layout.highlightuserdialog, null);
		    	b.setView(d);
		    	b.setTitle("Add Highlighted User");
		    	
		    	final EditText name = (EditText) d.findViewById(R.id.huName);
		    	final EditText label = (EditText) d.findViewById(R.id.huLabel);
		    	final TextView color = (TextView) d.findViewById(R.id.huColor);
		    	final Button setColor = (Button) d.findViewById(R.id.huSetColor);
		    	
		    	setColor.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int startColor = 0;
						if (!color.getText().equals("Color"))
							startColor = Integer.parseInt(color.getText().toString());
						
						new ColorSelectorDialog(SettingsHighlightedUsers.this,
								new OnColorChangedListener() {
									@Override
									public void colorChanged(int newColor) {
										color.setText(Integer.toString(newColor));
									}
								}, 
								startColor).show();
					}
				});
		    	
		    	b.setPositiveButton("Save", null);
		    	b.setNegativeButton("Cancel", null);
		    	
		    	
		    	final AlertDialog diag = b.create();
		    	diag.setOnShowListener(new OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						diag.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (name.getText().toString().length() > 0 && 
										label.getText().toString().length() > 0 && 
										!color.getText().equals("Color")) {
									
									hlDB.addUser(name.getText().toString(), 
											label.getText().toString(), Integer.parseInt(color.getText().toString()));
									
									updateList();
									diag.dismiss();
								}
								else {
									Toast.makeText(SettingsHighlightedUsers.this,
											"Missing required info", Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				});
		    	diag.show();
			}
		});
		
		wrapper.addView(addUser);
		
		setContentView(wrapper, lp);
		
		updateList();
	}
	
	private void updateList() {
		wrapper.removeAllViews();
		wrapper.addView(addUser);
		for (Entry<String, HighlightedUser> user : hlDB.getHighlightedUsers().entrySet()) {
			wrapper.addView(new HeaderView(this, user.getValue().getName() + 
					", " + user.getValue().getLabel() + ", " + user.getValue().getColor()));
		}
	}
}

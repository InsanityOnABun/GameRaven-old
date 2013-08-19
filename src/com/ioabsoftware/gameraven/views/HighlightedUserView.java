package com.ioabsoftware.gameraven.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

	public HighlightedUserView(SettingsHighlightedUsers hlActivityIn, HighlightedUser userIn) {
		super(hlActivityIn);
		hlActivity = hlActivityIn;
		
		LayoutInflater inflater = (LayoutInflater) hlActivityIn.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.highlighteduserview, this);
        
        user = userIn;
        
        nameView = (TextView) findViewById(R.id.hvName);
        labelView = (TextView) findViewById(R.id.hvLabel);
        colorFrame = (LinearLayout) findViewById(R.id.hvColorFrame);
        findViewById(R.id.hvSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        nameView.setText(user.getName());
        labelView.setText(user.getLabel());
        
        if (user.getColor() != 0)
        	colorFrame.setBackgroundColor(user.getColor());
        
        setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		HighlightListDBHelper.showHighlightUserDialog(hlActivity, user, null, hlActivity);
	}
	
	

}

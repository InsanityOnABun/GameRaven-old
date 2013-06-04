package com.ioabsoftware.gameraven;

import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.ioabsoftware.gameraven.db.HighlightListDBHelper;
import com.ioabsoftware.gameraven.db.HighlightedUser;
import com.ioabsoftware.gameraven.views.HighlightedUserView;

public class SettingsHighlightedUsers extends Activity {

	private LinearLayout wrapper;
	private HighlightedUserView addUser;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		if (AllInOneV2.getUsingLightTheme()) {
        	setTheme(R.style.MyThemes_LightTheme);
        }
		
		super.onCreate(savedInstanceState);
		
		wrapper = new LinearLayout(this);
		
		HighlightedUser newUser = new HighlightedUser(-1, "Add new highlighted user...", "Click to add new highlighted user.", 0);
		addUser = new HighlightedUserView(this, newUser);
		
		wrapper.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		setContentView(wrapper, lp);
		
		updateList();
	}
	
	public void updateList() {
		wrapper.removeAllViews();
		wrapper.addView(addUser);
		for (HighlightedUser user : AllInOneV2.getHLDB().getHighlightedUsers().values()) {
			wrapper.addView(new HighlightedUserView(this, user));
		}
	}
}

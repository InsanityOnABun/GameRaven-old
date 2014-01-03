package com.ioabsoftware.gameraven;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ioabsoftware.gameraven.db.HighlightedUser;
import com.ioabsoftware.gameraven.db.HlUDDismissListener;
import com.ioabsoftware.gameraven.views.rowview.HighlightedUserView;

public class SettingsHighlightedUsers extends Activity implements HlUDDismissListener {

	private LinearLayout wrapper;
	private HighlightedUserView addUser;
	private ScrollView scroller;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		if (AllInOneV2.getUsingLightTheme()) {
        	setTheme(R.style.MyThemes_LightTheme);
        }
		
		super.onCreate(savedInstanceState);
		
		Drawable aBarDrawable;
		if (AllInOneV2.getUsingLightTheme())
			aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_dark_holo);
		else
			aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_light_holo);
		
		aBarDrawable.setColorFilter(AllInOneV2.getAccentColor(), PorterDuff.Mode.SRC_ATOP);
		getActionBar().setBackgroundDrawable(aBarDrawable);
		
		wrapper = new LinearLayout(this);
		LinearLayout outerWrapper = new LinearLayout(this);
		
		scroller = new ScrollView(this);
		scroller.setVerticalFadingEdgeEnabled(true);
		
		HighlightedUser newUser = new HighlightedUser(-1, "Add new highlighted user...", "Click to add new highlighted user.", 0);
		addUser = new HighlightedUserView(this, newUser);
		
		wrapper.setOrientation(LinearLayout.VERTICAL);
		outerWrapper.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		scroller.addView(wrapper);
		outerWrapper.addView(addUser);
		outerWrapper.addView(scroller);
		setContentView(outerWrapper, lp);
		
		updateList();
	}
	
	public void updateList() {
		int scroll = scroller.getScrollY();
		wrapper.removeAllViews();
		for (HighlightedUser user : AllInOneV2.getHLDB().getHighlightedUsers().values()) {
			wrapper.addView(new HighlightedUserView(this, user));
		}
		scroller.scrollTo(0, scroll);
	}

	@Override
	public void beforeDismissSuccessfulSave() {
		updateList();
	}
}

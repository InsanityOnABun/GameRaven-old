package com.ioabsoftware.gameraven.prefs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.db.HighlightedUser;
import com.ioabsoftware.gameraven.db.HlUDDismissListener;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.rowview.HighlightedUserView;

public class SettingsHighlightedUsers extends ActionBarActivity implements HlUDDismissListener {

    private LinearLayout wrapper;
    private ScrollView scroller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Theming.theme());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_highlightedusers);

        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.hluToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        wrapper = new LinearLayout(this);
        LinearLayout outerWrapper = (LinearLayout) findViewById(R.id.hluOuterWrapper);

        scroller = new ScrollView(this);
        scroller.setVerticalFadingEdgeEnabled(true);

        HighlightedUser newUser = new HighlightedUser(-1, "Add new highlighted user...", "Click to add new highlighted user.", 0);
        HighlightedUserView addUser = new HighlightedUserView(this, newUser);

        wrapper.setOrientation(LinearLayout.VERTICAL);
        outerWrapper.setOrientation(LinearLayout.VERTICAL);

        scroller.addView(wrapper);
        outerWrapper.addView(addUser);
        outerWrapper.addView(scroller);

        Theming.colorOverscroll(this);

        updateList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

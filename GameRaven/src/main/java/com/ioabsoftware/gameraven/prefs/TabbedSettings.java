package com.ioabsoftware.gameraven.prefs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.ioabsoftware.gameraven.NotifierService;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.Theming;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class TabbedSettings extends Activity implements ActionBar.TabListener {

    public static final String NO_DEFAULT_ACCOUNT = "N/A";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter sectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager viewPager;

    SharedPreferences settings;

    public SharedPreferences getSettings() {
        return settings;
    }

    private PendingIntent notifPendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabbed_settings);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        Theming.colorOverscroll(this);

        // Set up notification pending intent
        Intent notifierIntent = new Intent(this, NotifierService.class);
        notifPendingIntent = PendingIntent.getService(this, 0, notifierIntent, 0);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        assert actionBar != null : "Action Bar is null";
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(sectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }
    }

    @Override
    public void onDestroy() {
        Crouton.clearCroutonsForActivity(this);
        super.onDestroy();
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            // getItem is called to instantiate the fragment for the given page.

            switch (position) {
                case 0:
                    return new PrefsAccountsNotifs();
                case 1:
                    return new PrefsGeneral();
                case 2:
                    return new PrefsTheming();
                case 3:
                    return new PrefsAdvanced();
                default:
                    throw new IllegalArgumentException("Settings fragment position " + position + " not recognized.");
            }
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Accounts & Notifications";
                case 1:
                    return "General";
                case 2:
                    return "Theming";
                case 3:
                    return "Advanced";
            }
            return null;
        }
    }

    public void enableNotifs(String freq) {
        long millis = 60000 * Integer.parseInt(freq);
        long firstAlarm = SystemClock.elapsedRealtime() + millis;
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstAlarm, millis, notifPendingIntent);
        settings.edit().putLong("notifsLastPost", 0).commit();
    }

    public void disableNotifs() {
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(notifPendingIntent);
    }

}

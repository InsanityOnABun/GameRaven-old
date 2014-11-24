package com.ioabsoftware.gameraven.prefs;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.Theming;

import java.util.List;

public class HeaderSettings extends PreferenceActivity {

    private Toolbar mActionBar;

    public static final String NO_DEFAULT_ACCOUNT = "N/A";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Theming.theme());
        super.onCreate(savedInstanceState);

        mActionBar.setTitle(getTitle());
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.settings_activity, new LinearLayout(this), false);

        mActionBar = (Toolbar) contentView.findViewById(R.id.saToolbar);
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.saContentWrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferenceheaders, target);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String settings = getArguments().getString("settings");
            if ("accountsnotifs".equals(settings)) {
                addPreferencesFromResource(R.xml.prefsaccountsnotifs);
            } else if ("theming".equals(settings)) {
                addPreferencesFromResource(R.xml.prefstheming);
            } else if ("general".equals(settings)) {
                addPreferencesFromResource(R.xml.prefsgeneral);
            } else if ("advanced".equals(settings)) {
                addPreferencesFromResource(R.xml.prefsadvanced);
            }
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }
}

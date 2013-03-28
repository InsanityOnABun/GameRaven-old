package com.ioabsoftware.DroidFAQs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.ioabsoftware.gameraven.R;

public class SettingsTheming extends SherlockPreferenceActivity {
	
	SharedPreferences settings;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		settings = AllInOneV2.getSettingsPref();
		if (AllInOneV2.getSettingsPref().getBoolean("useLightTheme", false)) {
        	setTheme(R.style.MyThemes_LightTheme);
        }
		
		super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settingstheming);
	}
}

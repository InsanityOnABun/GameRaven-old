package com.ioabsoftware.gameraven.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ioabsoftware.gameraven.R;

public class PrefsGeneral extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefsgeneral);
    }
}

package com.ioabsoftware.gameraven.prefs;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.ioabsoftware.gameraven.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class PrefsTheming extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefstheming);

        findPreference("manageHighlightedUsers").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SettingsHighlightedUsers.class));
                return true;
            }
        });

        findPreference("resetAccentColor").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Crouton.showText(getActivity(), "Accent color reset.", com.ioabsoftware.gameraven.util.Theming.croutonStyle());
                ((ColorPickerPreference) findPreference("accentColor")).onColorChanged(getResources().getColor(R.color.holo_blue));
                return false;
            }
        });
    }
}

package com.ioabsoftware.gameraven.prefs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.BuildConfig;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.Theming;

import org.apache.commons.lang3.StringEscapeUtils;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class PrefsAccountsNotifs extends PreferenceFragment {

    private TabbedSettings myHost;
    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefsaccountsnotifs);

        myHost = (TabbedSettings) getActivity();
        settings = myHost.getSettings();

        findPreference("manageAccounts").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SettingsAccount.class));
                return true;
            }
        });

        findPreference("customSig").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View v = inflater.inflate(R.layout.modifysig, null);
                b.setView(v);
                b.setTitle("Modify Global Custom Signature");

                final EditText sigText = (EditText) v.findViewById(R.id.sigEditText);
                final TextView sigCounter = (TextView) v.findViewById(R.id.sigCounter);
                final LinearLayout sigWrapper = (LinearLayout) v.findViewById(R.id.sigWrapper);

                sigText.setHint(AllInOneV2.defaultSig);
                sigText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        String escapedSig = StringEscapeUtils.escapeHtml4(sigText.getText().toString());
                        int length = escapedSig.length();
                        int lines = 0;
                        for (int i = 0; i < escapedSig.length(); i++) {
                            if (escapedSig.charAt(i) == '\n') lines++;
                        }

                        sigCounter.setText((1 - lines) + " line break(s), " + (160 - length) + " characters available");
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                    }
                });
                sigText.setText(settings.getString("customSig", ""));

                b.setPositiveButton("Save Sig", null);
                b.setNeutralButton("Clear Sig", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.edit().putString("customSig", "").apply();
                        Crouton.showText(getActivity(), "Signature cleared and saved.", Theming.croutonStyle());
                    }
                });
                b.setNegativeButton("Cancel", null);

                final AlertDialog d = b.create();
                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onShow(DialogInterface dialog) {
                        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                String escapedSig = StringEscapeUtils.escapeHtml4(sigText.getText().toString());
                                int length = escapedSig.length();
                                int lines = 0;
                                for (int i = 0; i < escapedSig.length(); i++) {
                                    if (escapedSig.charAt(i) == '\n') lines++;
                                }

                                if (length < 161) {
                                    if (lines < 2) {
                                        settings.edit().putString("customSig", sigText.getText().toString()).apply();
                                        Crouton.showText(getActivity(), "Signature saved.", Theming.croutonStyle());
                                        d.dismiss();
                                    } else
                                        Crouton.showText(getActivity(),
                                                "Signatures can only have 1 line break.",
                                                Theming.croutonStyle(),
                                                sigWrapper);
                                } else
                                    Crouton.showText(getActivity(),
                                            "Signatures can only have a maximum of 160 characters.",
                                            Theming.croutonStyle(),
                                            sigWrapper);
                            }
                        });
                    }
                });

                d.show();
                return true;
            }
        });

        findPreference("notifsEnable").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    // enabling notifications
                    if (settings.getString("defaultAccount", TabbedSettings.NO_DEFAULT_ACCOUNT).equals(TabbedSettings.NO_DEFAULT_ACCOUNT)) {
                        Crouton.showText(getActivity(), "You have no default account set!", Theming.croutonStyle());
                        return false;
                    } else {
                        myHost.enableNotifs(settings.getString("notifsFrequency", "60"));
                    }
                } else {
                    // disabling notifications
                    myHost.disableNotifs();
                }
                return true;
            }
        });

        findPreference("notifsFrequency").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                myHost.disableNotifs();
                myHost.enableNotifs((String) newValue);
                return true;
            }
        });

        // remove first notifsFrequency setting (1 min dev) if release build
        if (!BuildConfig.DEBUG) {
            ListPreference p = (ListPreference) findPreference("notifsFrequency");

            int size = p.getEntries().length;
            CharSequence[] entries = new String[size - 1];
            CharSequence[] vals = new String[size - 1];
            for (int x = 1; x < size; x++) {
                entries[x - 1] = p.getEntries()[x];
                vals[x - 1] = p.getEntryValues()[x];
            }

            p.setEntries(entries);
            p.setEntryValues(vals);
        }
    }
}

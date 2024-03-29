package com.ioabsoftware.gameraven.prefs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ioabsoftware.gameraven.About;
import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.BuildConfig;
import com.ioabsoftware.gameraven.NotifierService;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.db.HighlightedUser;
import com.ioabsoftware.gameraven.util.AccountManager;
import com.ioabsoftware.gameraven.util.Theming;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class HeaderSettings extends PreferenceActivity {

    public static final String NO_DEFAULT_ACCOUNT = "N/A";

    public static final ArrayList<String> ACCEPTED_KEYS = new ArrayList<>();

    private PendingIntent notifPendingIntent;
    public PendingIntent getNotifPendingIntent() {
        return notifPendingIntent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Theming.theme());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Theming.colorPrimaryDark());
        }

        super.onCreate(savedInstanceState);

        notifPendingIntent = PendingIntent.getService(this, 0, new Intent(this, NotifierService.class), 0);

        ACCEPTED_KEYS.add("timezone");
        ACCEPTED_KEYS.add("notifsEnable");
        ACCEPTED_KEYS.add("notifsAMPEnable"); // for backwards compatibility, no effect now
        ACCEPTED_KEYS.add("notifsTTEnable"); // for backwards compatibility, no effect now
        ACCEPTED_KEYS.add("notifsPMEnable"); // for backwards compatibility, no effect now
        ACCEPTED_KEYS.add("notifsFrequency");
        ACCEPTED_KEYS.add("reloadOnBack");
        ACCEPTED_KEYS.add("reloadOnResume");
        ACCEPTED_KEYS.add("enablePTR");
        ACCEPTED_KEYS.add("enableFastScroll");
        ACCEPTED_KEYS.add("defaultAccount");
        ACCEPTED_KEYS.add("grBackupVer");
        ACCEPTED_KEYS.add("startAtAMP");
        ACCEPTED_KEYS.add("ampSortOption");
        ACCEPTED_KEYS.add("confirmPostCancel");
        ACCEPTED_KEYS.add("confirmPostSubmit");
        ACCEPTED_KEYS.add("textScale");
        ACCEPTED_KEYS.add("usingAvatars");
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.settings_activity, new LinearLayout(this), false);

        Toolbar mActionBar = (Toolbar) contentView.findViewById(R.id.saToolbar);
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mActionBar.setTitle(getTitle());
        mActionBar.setTitleTextColor(Color.WHITE);

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.saContentWrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferenceheaders, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    private static final int PERMISSION_READ_EXTERNAL = 0;
    private static final int PERMISSION_WRITE_EXTERNAL = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                    restoreSettings();
                } else {
                    Toast.makeText(this, "You denied permission to read from storage.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSION_WRITE_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                    backupSettings();
                } else {
                    Toast.makeText(this, "You denied permission to read from storage.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void enableNotifs(String freq) {
        long millis = 60000 * Integer.parseInt(freq);
        long firstAlarm = SystemClock.elapsedRealtime() + 1000; // first notifier run should be in one second
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE))
                .setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstAlarm, millis, this.getNotifPendingIntent());
        AllInOneV2.getSettingsPref().edit().putLong("notifsLastPost", 0).apply();
    }

    public void disableNotifs() {
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).cancel(this.getNotifPendingIntent());
    }

    private void backupSettings() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            File settingsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gameraven",
                    "gameraven_settings");

            try {

                //noinspection ResultOfMethodCallIgnored
                settingsFile.delete();
                //noinspection ResultOfMethodCallIgnored
                settingsFile.createNewFile();

                BufferedWriter buf = new BufferedWriter(new FileWriter(
                        settingsFile, true));

                buf.append("[ACCOUNTS]\n");
                for (String s : AccountManager.getUsernames(this)) {

                    buf.append(s).append("\n");
                    buf.append(AccountManager.getPassword(this, s)).append("\n");
                    buf.append(String.valueOf(AllInOneV2.getSettingsPref().getBoolean("useGFAQsSig" + s, false)));

                    buf.append("[CUSTOM_SIG]\n");
                    buf.append(AllInOneV2.getSettingsPref().getString("customSig" + s, "")).append('\n');
                    buf.append("[END_CUSTOM_SIG]\n");
                }
                buf.append("[END_ACCOUNTS]\n");

                buf.append("[GLOBAL_SIG]\n");
                buf.append(AllInOneV2.getSettingsPref().getString("customSig", "")).append('\n');
                buf.append("[END_GLOBAL_SIG]\n");

                buf.append("[HIGHLIGHT_LIST]\n");
                for (HighlightedUser user : AllInOneV2.getHLDB().getHighlightedUsers().values()) {
                    buf.append(user.getName()).append("\n")
                            .append(user.getLabel()).append("\n")
                            .append(String.valueOf(user.getColor())).append("\n");
                }
                buf.append("[END_HIGHLIGHT_LIST]\n");

                buf.append("defaultAccount=").append(AllInOneV2.getSettingsPref().getString("defaultAccount", NO_DEFAULT_ACCOUNT)).append('\n');

                buf.append("timezone=").append(AllInOneV2.getSettingsPref().getString("timezone", TimeZone.getDefault().getID())).append('\n');

                if (AllInOneV2.getSettingsPref().getBoolean("notifsEnable", false))
                    buf.append("notifsEnable=true\n");
                else
                    buf.append("notifsEnable=false\n");

                buf.append("notifsFrequency=").append(AllInOneV2.getSettingsPref().getString("notifsFrequency", "60")).append('\n');

                if (AllInOneV2.getSettingsPref().getBoolean("usingAvatars", false))
                    buf.append("usingAvatars=true\n");
                else
                    buf.append("usingAvatars=false\n");

                if (AllInOneV2.getSettingsPref().getBoolean("startAtAMP", false))
                    buf.append("startAtAMP=true\n");
                else
                    buf.append("startAtAMP=false\n");

                if (AllInOneV2.getSettingsPref().getBoolean("reloadOnBack", false))
                    buf.append("reloadOnBack=true\n");
                else
                    buf.append("reloadOnBack=false\n");

                if (AllInOneV2.getSettingsPref().getBoolean("reloadOnResume", false))
                    buf.append("reloadOnResume=true\n");
                else
                    buf.append("reloadOnResume=false\n");

                if (AllInOneV2.getSettingsPref().getBoolean("enablePTR", false))
                    buf.append("enablePTR=true\n");
                else
                    buf.append("enablePTR=false\n");

                if (AllInOneV2.getSettingsPref().getBoolean("enableFastScroll", true))
                    buf.append("enableFastScroll=true\n");
                else
                    buf.append("enableFastScroll=false\n");

                buf.append("textScale=").append(String.valueOf(AllInOneV2.getSettingsPref().getInt("textScale", 100))).append('\n');

                buf.append("ampSortOption=").append(AllInOneV2.getSettingsPref().getString("ampSortOption", "-1")).append('\n');

                if (AllInOneV2.getSettingsPref().getBoolean("confirmPostCancel", false))
                    buf.append("confirmPostCancel=true\n");
                else
                    buf.append("confirmPostCancel=false\n");

                if (AllInOneV2.getSettingsPref().getBoolean("confirmPostSubmit", false))
                    buf.append("confirmPostSubmit=true\n");
                else
                    buf.append("confirmPostSubmit=false\n");

                buf.close();
                Toast.makeText(this,"Backup done." ,Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            Log.e("writeToLog", "error writing to log, external storage is not writable");
            Toast.makeText(this,"Backup failed. Storage is most likely not accessible." ,Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreSettings() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            File settingsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gameraven",
                    "gameraven_settings");

            if (settingsFile.exists()) {
                try {
                    SharedPreferences.Editor editor = AllInOneV2.getSettingsPref().edit();
                    BufferedReader br = new BufferedReader(new FileReader(settingsFile));
                    String line;
                    String[] splitLine;
                    ArrayList<String> users = new ArrayList<>();
                    ArrayList<String> passwords = new ArrayList<>();
                    ArrayList<String> keys = new ArrayList<>();
                    ArrayList<String> values = new ArrayList<>();

                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("//")) {

                            if (line.startsWith("[")) {

                                if (line.equals("[ACCOUNTS]")) {
                                    while (!(line = br.readLine()).equals("[END_ACCOUNTS]")) {
                                        String user = line;

                                        users.add(user);
                                        passwords.add(br.readLine());
                                        keys.add("useGFAQsSig" + user);
                                        values.add(br.readLine());

                                        br.readLine();
                                        String sig = "";
                                        boolean isFirstLine = true;
                                        while (!(line = br.readLine()).equals("[END_CUSTOM_SIG]")) {
                                            if (!isFirstLine)
                                                sig += '\n';

                                            sig += line;
                                            isFirstLine = false;
                                        }

                                        keys.add("customSig" + user);
                                        values.add(sig);

                                    }
                                } else if (line.equals("[GLOBAL_SIG]")) {
                                    String globalSig = "";
                                    boolean isFirstLine = true;
                                    while (!(line = br.readLine()).equals("[END_GLOBAL_SIG]")) {
                                        if (!isFirstLine)
                                            globalSig += '\n';

                                        globalSig += line;
                                        isFirstLine = false;
                                    }
                                    keys.add("customSig");
                                    values.add(globalSig);
                                } else if (line.equals("[HIGHLIGHT_LIST]")) {
                                    while (!(line = br.readLine()).equals("[END_HIGHLIGHT_LIST]")) {
                                        String label = br.readLine();
                                        String color = br.readLine();
                                        if (AllInOneV2.getHLDB().hasUser(line))
                                            AllInOneV2.getHLDB().deleteUser(line);

                                        AllInOneV2.getHLDB().addUser(line, label, Integer.parseInt(color));
                                    }
                                } else
                                if (BuildConfig.DEBUG) AllInOneV2.wtl("line unhandled in restore: " + line);
                            } else if (line.contains("=")) {
                                splitLine = line.split("=", 2);
                                keys.add(splitLine[0]);
                                values.add(splitLine[1]);
                            } else
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("line unhandled in restore: " + line);
                        }
                    }

                    br.close();

                    // clear accs before adding in restored ones
                    AccountManager.clearAccounts(this);

                    for (int a = 0; a < users.size(); a++) {
                        AccountManager.addUser(this, users.get(a), passwords.get(a));
                    }

                    for (int x = 0; x < keys.size(); x++) {
                        String key = keys.get(x);
                        String val = values.get(x);

                        if (ACCEPTED_KEYS.contains(key) || key.startsWith("customSig")) {
                            if (val.equals("true") || val.equals("false")) {
                                editor.putBoolean(key, Boolean.parseBoolean(val));
                            } else if (isInteger(val)) {
                                if (key.equals("ampSortOption") || key.equals("notifsFrequency"))
                                    editor.putString(key, val);
                                else
                                    editor.putInt(key, Integer.parseInt(val));
                            } else
                                editor.putString(key, val);
                        } else
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("Key, Val pair not recognized in restore: " + key + ", " + val);
                    }

                    editor.apply();

                    disableNotifs();
                    if (AllInOneV2.getSettingsPref().getBoolean("notifsEnable", false))
                        enableNotifs(AllInOneV2.getSettingsPref().getString("notifsFrequency", "60"));

                    Toast.makeText(this, "Restore done.", Toast.LENGTH_SHORT).show();
                    this.finish();
                    startActivity(this.getIntent());

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Settings file is corrupt.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Settings file not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            Log.e("writeToLog", "error writing to log, external storage is not writable");
            Toast.makeText(this, "Restore failed. Storage is most likely not accessible.", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') {
                return false;
            }
        }
        return true;
    }





    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String settings = getArguments().getString("settings");
            /**
             * ACCOUNT & NOTIFS SETTINGS
             */
            if ("accountsnotifs".equals(settings)) {
                addPreferencesFromResource(R.xml.prefsaccountsnotifs);
                findPreference("manageAccounts").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), SettingsAccount.class));
                        return true;
                    }
                });
                findPreference("customSig").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
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
                        sigText.setText(AllInOneV2.getSettingsPref().getString("customSig", ""));

                        b.setPositiveButton("Save Sig", null);
                        b.setNeutralButton("Clear Sig", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AllInOneV2.getSettingsPref().edit().putString("customSig", "").apply();
                                Toast.makeText(getActivity(), "Signature cleared and saved.", Toast.LENGTH_SHORT).show();
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
                                                AllInOneV2.getSettingsPref().edit().putString("customSig", sigText.getText().toString()).apply();
                                                Toast.makeText(getActivity(), "Signature saved.", Toast.LENGTH_SHORT).show();
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
                            if (AllInOneV2.getSettingsPref().getString("defaultAccount", NO_DEFAULT_ACCOUNT).equals(NO_DEFAULT_ACCOUNT)) {
                                Crouton.showText(getActivity(), "You have no default account set!", Theming.croutonStyle(), (ViewGroup) getView());
                                return false;
                            } else {
                                ((HeaderSettings)getActivity()).enableNotifs(
                                        AllInOneV2.getSettingsPref().getString("notifsFrequency", "60"));
                            }
                        } else {
                            // disabling notifications
                            ((HeaderSettings)getActivity()).disableNotifs();
                        }
                        return true;
                    }
                });

                findPreference("notifsFrequency").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ((HeaderSettings)getActivity()).disableNotifs();
                        ((HeaderSettings)getActivity()).enableNotifs((String) newValue);
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
            /**
             * THEMING SETTINGS
             */
            else if ("theming".equals(settings)) {
                addPreferencesFromResource(R.xml.prefstheming);
                findPreference("manageHighlightedUsers").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), SettingsHighlightedUsers.class));
                        return true;
                    }
                });
                findPreference("gfTheme").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    @SuppressLint("SetTextI18n")
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        final View v = inflater.inflate(R.layout.themepicker, null);
                        b.setView(v);
                        b.setTitle("Select Theme");

                        final TextView current = (TextView) v.findViewById(R.id.tpSelected);

                        v.findViewById(R.id.tpBlueLight).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Light Blue").apply();
                                current.setText("Selected: Light Blue");
                            }
                        });
                        v.findViewById(R.id.tpBlueDark).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Dark Blue").apply();
                                current.setText("Selected: Dark Blue");
                            }
                        });
                        v.findViewById(R.id.tpRedLight).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Light Red").apply();
                                current.setText("Selected: Light Red");
                            }
                        });
                        v.findViewById(R.id.tpRedDark).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Dark Red").apply();
                                current.setText("Selected: Dark Red");
                            }
                        });
                        v.findViewById(R.id.tpGreenLight).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Light Green").apply();
                                current.setText("Selected: Light Green");
                            }
                        });
                        v.findViewById(R.id.tpGreenDark).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Dark Green").apply();
                                current.setText("Selected: Dark Green");
                            }
                        });
                        v.findViewById(R.id.tpOrangeLight).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Light Orange").apply();
                                current.setText("Selected: Light Orange");
                            }
                        });
                        v.findViewById(R.id.tpOrangeDark).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Dark Orange").apply();
                                current.setText("Selected: Dark Orange");
                            }
                        });
                        v.findViewById(R.id.tpPurpleLight).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Light Purple").apply();
                                current.setText("Selected: Light Purple");
                            }
                        });
                        v.findViewById(R.id.tpPurpleDark).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AllInOneV2.getSettingsPref().edit().putString("gfTheme", "Dark Purple").apply();
                                current.setText("Selected: Dark Purple");
                            }
                        });

                        current.setText("Selected: " + AllInOneV2.getSettingsPref().getString("gfTheme", "Light Blue"));

                        b.setPositiveButton("OK", null);

                        b.show();
                        return true;
                    }
                });
            }
            /**
             * GENERAL SETTINGS
             */
            else if ("general".equals(settings)) {
                addPreferencesFromResource(R.xml.prefsgeneral);
                findPreference("backupSettings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder bb = new AlertDialog.Builder(getActivity());
                        bb.setTitle("Backup Settings");
                        bb.setMessage("Are you sure you want to back up your settings? This will " +
                                "overwrite any previous backup, and passwords are stored as plain " +
                                "text. This also requires permission to write to the SD card, which " +
                                "will be requested if necessary when you press yes.");
                        bb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                                    ((HeaderSettings)getActivity()).backupSettings();
                                else
                                    checkForWritePermissions();
                            }
                        });
                        bb.setNegativeButton("Cancel", null);
                        bb.create().show();
                        return true;
                    }
                });

                findPreference("restoreSettings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder rb = new AlertDialog.Builder(getActivity());
                        rb.setTitle("Restore Settings");
                        rb.setMessage("Are you sure you want to restore your settings? This will " +
                                "wipe any previously added accounts. This also requires permission " +
                                "to read from the SD card, which will be requested if necessary " +
                                "when you press yes.");
                        rb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                                    ((HeaderSettings)getActivity()).restoreSettings();
                                else
                                    checkForReadPermissions();
                            }
                        });
                        rb.setNegativeButton("Cancel", null);
                        rb.create().show();
                        return true;
                    }
                });

                findPreference("aboutFeedback").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), About.class));
                        return true;
                    }
                });
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void checkForReadPermissions() {
            if (ContextCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_READ_EXTERNAL);
            } else
                ((HeaderSettings)getActivity()).restoreSettings();
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void checkForWritePermissions() {
            if (ContextCompat.checkSelfPermission(this.getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_EXTERNAL);
            } else
                ((HeaderSettings)getActivity()).backupSettings();
        }
    }
}

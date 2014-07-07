package com.ioabsoftware.gameraven.prefs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.ioabsoftware.gameraven.About;
import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.BuildConfig;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.db.HighlightedUser;
import com.ioabsoftware.gameraven.util.AccountManager;
import com.ioabsoftware.gameraven.util.Theming;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimeZone;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class PrefsAdvanced extends PreferenceFragment {

    private TabbedSettings myHost;
    private SharedPreferences settings;

    final ArrayList<String> ACCEPTED_KEYS = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settingsadvanced);

        myHost = (TabbedSettings) getActivity();
        settings = myHost.getSettings();

        ACCEPTED_KEYS.add("timezone");
        ACCEPTED_KEYS.add("notifsEnable");
        ACCEPTED_KEYS.add("notifsAMPEnable");
        ACCEPTED_KEYS.add("notifsTTEnable");
        ACCEPTED_KEYS.add("notifsPMEnable");
        ACCEPTED_KEYS.add("notifsFrequency");
        ACCEPTED_KEYS.add("reloadOnBack");
        ACCEPTED_KEYS.add("reloadOnResume");
        ACCEPTED_KEYS.add("enablePTR");
        ACCEPTED_KEYS.add("defaultAccount");
        ACCEPTED_KEYS.add("grBackupVer");
        ACCEPTED_KEYS.add("startAtAMP");
        ACCEPTED_KEYS.add("useLightTheme");
        ACCEPTED_KEYS.add("useWhiteAccentText");
        ACCEPTED_KEYS.add("accentColor");
        ACCEPTED_KEYS.add("enableJS");
        ACCEPTED_KEYS.add("ampSortOption");
        ACCEPTED_KEYS.add("confirmPostCancel");
        ACCEPTED_KEYS.add("confirmPostSubmit");
        ACCEPTED_KEYS.add("autoCensorEnable");
        ACCEPTED_KEYS.add("textScale");
        ACCEPTED_KEYS.add("usingAvatars");

        findPreference("backupSettings").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder bb = new AlertDialog.Builder(getActivity());
                bb.setTitle("Backup Settings");
                bb.setMessage("Are you sure you want to back up your settings? This will overwrite " +
                        "any previous backup, and passwords are stored as plain text.");
                bb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        backupSettings();
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
                rb.setMessage("Are you sure you want to restore your settings? This will wipe any previously added accounts.");
                rb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restoreSettings();
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
                for (String s : AccountManager.getUsernames(getActivity())) {

                    buf.append(s).append("\n");
                    buf.append(AccountManager.getPassword(getActivity(), s)).append("\n");

                    buf.append("[CUSTOM_SIG]\n");
                    buf.append(settings.getString("customSig" + s, "")).append('\n');
                    buf.append("[END_CUSTOM_SIG]\n");
                }
                buf.append("[END_ACCOUNTS]\n");

                buf.append("[GLOBAL_SIG]\n");
                buf.append(settings.getString("customSig", "")).append('\n');
                buf.append("[END_GLOBAL_SIG]\n");

                buf.append("[HIGHLIGHT_LIST]\n");
                for (HighlightedUser user : AllInOneV2.getHLDB().getHighlightedUsers().values()) {
                    buf.append(user.getName()).append("\n")
                            .append(user.getLabel()).append("\n")
                            .append(String.valueOf(user.getColor())).append("\n");
                }
                buf.append("[END_HIGHLIGHT_LIST]\n");

                buf.append("defaultAccount=").append(settings.getString("defaultAccount", TabbedSettings.NO_DEFAULT_ACCOUNT)).append('\n');

                buf.append("timezone=").append(settings.getString("timezone", TimeZone.getDefault().getID())).append('\n');

                if (settings.getBoolean("notifsEnable", false))
                    buf.append("notifsEnable=true\n");
                else
                    buf.append("notifsEnable=false\n");

                if (settings.getBoolean("notifsAMPEnable", false))
                    buf.append("notifsAMPEnable=true\n");
                else
                    buf.append("notifsAMPEnable=false\n");

                if (settings.getBoolean("notifsTTEnable", false))
                    buf.append("notifsTTEnable=true\n");
                else
                    buf.append("notifsTTEnable=false\n");

                if (settings.getBoolean("notifsPMEnable", false))
                    buf.append("notifsPMEnable=true\n");
                else
                    buf.append("notifsPMEnable=false\n");

                buf.append("notifsFrequency=").append(settings.getString("notifsFrequency", "60")).append('\n');

                if (settings.getBoolean("usingAvatars", false))
                    buf.append("usingAvatars=true\n");
                else
                    buf.append("usingAvatars=false\n");

                if (settings.getBoolean("startAtAMP", false))
                    buf.append("startAtAMP=true\n");
                else
                    buf.append("startAtAMP=false\n");

                if (settings.getBoolean("reloadOnBack", false))
                    buf.append("reloadOnBack=true\n");
                else
                    buf.append("reloadOnBack=false\n");

                if (settings.getBoolean("reloadOnResume", false))
                    buf.append("reloadOnResume=true\n");
                else
                    buf.append("reloadOnResume=false\n");

                if (settings.getBoolean("enablePTR", false))
                    buf.append("enablePTR=true\n");
                else
                    buf.append("enablePTR=false\n");

                buf.append("textScale=").append(String.valueOf(settings.getInt("textScale", 100))).append('\n');

                if (settings.getBoolean("useLightTheme", false))
                    buf.append("useLightTheme=true\n");
                else
                    buf.append("useLightTheme=false\n");

                if (settings.getBoolean("useWhiteAccentText", false))
                    buf.append("useWhiteAccentText=true\n");
                else
                    buf.append("useWhiteAccentText=false\n");

                String accent = String.valueOf(settings.getInt("accentColor", getResources().getColor(R.color.holo_blue)));
                buf.append("accentColor=").append(accent).append('\n');

                if (settings.getBoolean("enableJS", true))
                    buf.append("enableJS=true\n");
                else
                    buf.append("enableJS=false\n");

                buf.append("ampSortOption=").append(settings.getString("ampSortOption", "-1")).append('\n');

                if (settings.getBoolean("confirmPostCancel", false))
                    buf.append("confirmPostCancel=true\n");
                else
                    buf.append("confirmPostCancel=false\n");

                if (settings.getBoolean("confirmPostSubmit", false))
                    buf.append("confirmPostSubmit=true\n");
                else
                    buf.append("confirmPostSubmit=false\n");

                if (settings.getBoolean("autoCensorEnable", true))
                    buf.append("autoCensorEnable=true\n");
                else
                    buf.append("autoCensorEnable=false\n");

                buf.close();
                Crouton.showText(getActivity(), "Backup done.", Theming.croutonStyle());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            Log.e("writeToLog", "error writing to log, external storage is not writable");
            Crouton.showText(getActivity(), "Backup failed. External storage is most likely not accessible.", Theming.croutonStyle());
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
                    SharedPreferences.Editor editor = settings.edit();
                    BufferedReader br = new BufferedReader(new FileReader(settingsFile));
                    String line;
                    String[] splitLine;
                    ArrayList<String> users = new ArrayList<String>();
                    ArrayList<String> passwords = new ArrayList<String>();
                    ArrayList<String> keys = new ArrayList<String>();
                    ArrayList<String> values = new ArrayList<String>();

                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("//")) {

                            if (line.startsWith("[")) {

                                if (line.equals("[ACCOUNTS]")) {
                                    while (!(line = br.readLine()).equals("[END_ACCOUNTS]")) {
                                        String user = line;

                                        users.add(user);
                                        passwords.add(br.readLine());

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
                    AccountManager.clearAccounts(getActivity());

                    for (int a = 0; a < users.size(); a++) {
                        AccountManager.addUser(getActivity(), users.get(a), passwords.get(a));
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

                    myHost.disableNotifs();
                    if (settings.getBoolean("notifsEnable", false))
                        myHost.enableNotifs(settings.getString("notifsFrequency", "60"));

//					Crouton.showText(this, "Restore done.", MainActivity.getCroutonStyle());
                    Toast.makeText(getActivity(), "Restore done.", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    startActivity(getActivity().getIntent());

                } catch (IOException e) {
                    e.printStackTrace();
                    Crouton.showText(getActivity(), "Settings file is corrupt.", Theming.croutonStyle());
                }
            } else {
                Crouton.showText(getActivity(), "Settings file not found.", Theming.croutonStyle());
            }
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            Log.e("writeToLog", "error writing to log, external storage is not writable");
            Crouton.showText(getActivity(), "Restore failed. External storage is most likely not accessible.", Theming.croutonStyle());
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
}

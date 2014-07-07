package com.ioabsoftware.gameraven;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.networking.Session;
import com.ioabsoftware.gameraven.prefs.TabbedSettings;
import com.ioabsoftware.gameraven.util.AccountManager;
import com.ioabsoftware.gameraven.util.DocumentParser;
import com.ioabsoftware.gameraven.util.FinalDoc;
import com.ioabsoftware.gameraven.util.Theming;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class SettingsAccount extends PreferenceActivity implements FutureCallback<Response<FinalDoc>> {

    private static final String ION_INSTANCE = "AccountVerifier";
    private Ion accountVerifier;

    public static final int ADD_ACCOUNT_DIALOG = 300;
    public static final int VERIFY_ACCOUNT_DIALOG = 301;
    public static final int MODIFY_ACCOUNT_DIALOG = 302;

    String verifyUser;
    String verifyPass;

    PreferenceCategory accounts;
    Preference clickedAccount;

    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (Theming.usingLightTheme()) {
            setTheme(R.style.MyThemes_LightTheme);
        }

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settingsaccounts);

        accountVerifier = Ion.getInstance(this, ION_INSTANCE);
        accountVerifier.getCookieMiddleware().clear();

        Theming.colorOverscroll(this);

        Drawable aBarDrawable;
        if (Theming.usingLightTheme())
            aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_dark_holo);
        else
            aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_light_holo);

        aBarDrawable.setColorFilter(Theming.accentColor(), PorterDuff.Mode.SRC_ATOP);
        getActionBar().setBackgroundDrawable(aBarDrawable);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        accounts = (PreferenceCategory) findPreference("accounts");
        updateAccountList();

        findPreference("addAccount").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showDialog(ADD_ACCOUNT_DIALOG);
                return true;
            }

        });
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
    public void onDestroy() {
        Crouton.clearCroutonsForActivity(this);
        super.onDestroy();
    }

    private void updateAccountList() {
        accounts.removeAll();

        String def = settings.getString("defaultAccount", TabbedSettings.NO_DEFAULT_ACCOUNT);

        for (String s : AccountManager.getUsernames(this)) {
            Preference pref = new Preference(this);
            pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    clickedAccount = preference;
                    showDialog(MODIFY_ACCOUNT_DIALOG);
                    return true;
                }
            });

            String sig = settings.getString("customSig" + s, "");

            pref.setTitle(s);

            if (s.equals(def) && !sig.equals(""))
                pref.setSummary("Default account, custom signature applied");
            else if (s.equals(def))
                pref.setSummary("Default account");
            else if (!sig.equals(""))
                pref.setSummary("Custom signature applied");

            pref.setPersistent(false);

            accounts.addPreference(pref);
        }
    }

    // creates dialogs
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case ADD_ACCOUNT_DIALOG:
                dialog = createAddAccountDialog();
                break;
            case VERIFY_ACCOUNT_DIALOG:
                ProgressDialog d = new ProgressDialog(this);
                d.setTitle("Verifying Account...");
                d.setCancelable(false);
                dialog = d;
                break;
            case MODIFY_ACCOUNT_DIALOG:
                dialog = createModifyAccountV2Dialog();
                break;
        }
        return dialog;
    }

    private LinearLayout addAccWrapper;

    private Dialog createAddAccountDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.addaccount, null);

        addAccWrapper = (LinearLayout) v.findViewById(R.id.addaccWrapper);

        b.setView(v);
        b.setTitle("Add Account");

        b.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(ADD_ACCOUNT_DIALOG);
            }
        });

        b.setPositiveButton("OK", null);

        final AlertDialog d = b.create();
        d.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        verifyUser = ((TextView) v.findViewById(R.id.addaccUser)).getText().toString().trim();
                        verifyPass = ((TextView) v.findViewById(R.id.addaccPassword)).getText().toString();

                        if (verifyUser.indexOf('@') == -1) {
							showDialog(VERIFY_ACCOUNT_DIALOG);

                            accountVerifier.getCookieMiddleware().clear();
                            currentDesc = NetDesc.VERIFY_ACCOUNT_S1;
                            accountVerifier.build(SettingsAccount.this)
                                    .load("GET", Session.ROOT)
                                    .as(new DocumentParser())
                                    .withResponse()
                                    .setCallback(SettingsAccount.this);
                        } else {
                            Crouton.showText(SettingsAccount.this,
                                    "Please use your username, not your email address.",
                                    Theming.croutonStyle(),
                                    (ViewGroup) v.findViewById(R.id.addaccUser).getParent());
                        }
                    }
                });
            }
        });

        d.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                addAccWrapper = null;
                removeDialog(ADD_ACCOUNT_DIALOG);
            }
        });

        return d;
    }

    private Dialog createModifyAccountV2Dialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.modifyaccountv2, null);
        b.setView(v);
        b.setTitle("Modify " + clickedAccount.getTitle().toString());

        Button deleteAcc = (Button) v.findViewById(R.id.modaccDeleteAcc);
        final CheckBox defaultAcc = (CheckBox) v.findViewById(R.id.modaccDefaultAccount);
        final EditText sigContent = (EditText) v.findViewById(R.id.modaccSigContent);
        final TextView sigCounter = (TextView) v.findViewById(R.id.modaccSigCounter);

        if (clickedAccount.getTitle().toString().equals(settings.getString("defaultAccount", TabbedSettings.NO_DEFAULT_ACCOUNT)))
            defaultAcc.setChecked(true);
        else
            defaultAcc.setChecked(false);

        defaultAcc.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.edit().putString("defaultAccount", clickedAccount.getTitle().toString()).commit();
                    Crouton.showText(SettingsAccount.this,
                            "Default account saved.",
                            Theming.croutonStyle(),
                            (ViewGroup) buttonView.getParent().getParent());
                } else {
                    settings.edit().putString("defaultAccount", TabbedSettings.NO_DEFAULT_ACCOUNT).commit();
                    settings.edit().putLong("notifsLastPost", 0).commit();
                    Crouton.showText(SettingsAccount.this,
                            "Default account removed.",
                            Theming.croutonStyle(),
                            (ViewGroup) buttonView.getParent().getParent());
                }
            }
        });

        deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickedAccount.getTitle().toString().equals(settings.getString("defaultAccount", TabbedSettings.NO_DEFAULT_ACCOUNT)))
                    settings.edit().putString("defaultAccount", TabbedSettings.NO_DEFAULT_ACCOUNT).commit();

                settings.edit().remove("customSig" + clickedAccount.getTitle().toString()).commit();

                AccountManager.removeUser(SettingsAccount.this, clickedAccount.getTitle().toString());
                accounts.removePreference(clickedAccount);
                dismissDialog(MODIFY_ACCOUNT_DIALOG);
                Crouton.showText(SettingsAccount.this, "Account removed.", Theming.croutonStyle());
            }
        });

        sigContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String escapedSig = StringEscapeUtils.escapeHtml4(sigContent.getText().toString());
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

        sigContent.setText(settings.getString("customSig" + clickedAccount.getTitle().toString(), ""));

        b.setPositiveButton("Save Sig", null);

        b.setNeutralButton("Clear Sig", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                settings.edit().putString("customSig" + clickedAccount.getTitle().toString(), "").commit();
                sigContent.setText("");
                Crouton.showText(SettingsAccount.this, "Signature cleared and saved.", Theming.croutonStyle());
            }
        });

        b.setNegativeButton("Close", null);

        final AlertDialog d = b.create();

        d.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String escapedSig = StringEscapeUtils.escapeHtml4(sigContent.getText().toString());
                        int length = escapedSig.length();
                        int lines = 0;
                        for (int i = 0; i < escapedSig.length(); i++) {
                            if (escapedSig.charAt(i) == '\n') lines++;
                        }

                        if (length < 161) {
                            if (lines < 2) {
                                settings.edit().putString("customSig" + clickedAccount.getTitle().toString(),
                                        sigContent.getText().toString()).commit();

                                Crouton.showText(SettingsAccount.this, "Signature saved.", Theming.croutonStyle());
                                d.dismiss();
                            } else {
                                Crouton.showText(SettingsAccount.this,
                                        "Signatures can only have 1 line break.",
                                        Theming.croutonStyle(),
                                        (ViewGroup) sigContent.getParent());
                            }
                        } else {
                            Crouton.showText(SettingsAccount.this,
                                    "Signatures can only have a maximum of 160 characters.",
                                    Theming.croutonStyle(),
                                    (ViewGroup) sigContent.getParent());
                        }
                    }
                });
            }
        });

        d.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateAccountList();
                removeDialog(MODIFY_ACCOUNT_DIALOG);
            }
        });

        return d;
    }

    private NetDesc currentDesc;
    /**
     * onCompleted is called by the Future with the result or exception of the asynchronous operation.
     *
     * @param e      Exception encountered by the operation
     * @param result Result returned from the operation
     */
    @Override
    public void onCompleted(Exception e, Response<FinalDoc> result) {
        if (e == null && result != null) {
            Document doc = result.getResult().doc;

            if (currentDesc == NetDesc.VERIFY_ACCOUNT_S1) {
                String loginKey = doc.getElementsByAttributeValue("name", "key").attr("value");
                HashMap<String, List<String>> loginData = new HashMap<String, List<String>>();
                // "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
                loginData.put("EMAILADDR", Arrays.asList(verifyUser));
                loginData.put("PASSWORD", Arrays.asList(verifyPass));
                loginData.put("path", Arrays.asList(Session.ROOT));
                loginData.put("key", Arrays.asList(loginKey));

                currentDesc = NetDesc.VERIFY_ACCOUNT_S2;
                accountVerifier.build(this)
                        .load("POST", Session.ROOT)
                        .setBodyParameters(loginData)
                        .as(new DocumentParser())
                        .withResponse()
                        .setCallback(SettingsAccount.this);
            }
            else if (currentDesc == NetDesc.VERIFY_ACCOUNT_S2) {
                if (!result.getRequest().getUri().toString().equals(Session.ROOT + "/user/login.html")) {
                    AccountManager.addUser(SettingsAccount.this, verifyUser, verifyPass);
                    dismissDialog(VERIFY_ACCOUNT_DIALOG);
                    removeDialog(ADD_ACCOUNT_DIALOG);
                    Crouton.showText(this, "Verification succeeded.", Theming.croutonStyle());
                    updateAccountList();
                }
                else {
                    dismissDialog(VERIFY_ACCOUNT_DIALOG);
                    Crouton.showText(this,
                            "Verification failed. Check your username and password and try again.",
                            Theming.croutonStyle(),
                            addAccWrapper);
                }
            }
        }
        else {
            dismissDialog(VERIFY_ACCOUNT_DIALOG);
            Crouton.showText(this, "Network connection failed. Check your network settings.", Theming.croutonStyle(), addAccWrapper);
        }
    }
}

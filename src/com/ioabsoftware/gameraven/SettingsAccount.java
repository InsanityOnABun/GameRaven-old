package com.ioabsoftware.gameraven;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.widget.Toast;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult;
import com.ioabsoftware.gameraven.networking.NetworkTask;
import com.ioabsoftware.gameraven.networking.Session;

public class SettingsAccount extends SherlockPreferenceActivity implements HandlesNetworkResult {

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
		settings = AllInOneV2.getSettingsPref();
		if (AllInOneV2.getSettingsPref().getBoolean("useLightTheme", false)) {
        	setTheme(R.style.MyThemes_LightTheme);
        }
		
		super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settingsaccount);

        accounts = (PreferenceCategory) findPreference("accounts");
		updateAccountList();
        
        findPreference("addAccount").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    showDialog(ADD_ACCOUNT_DIALOG);
                    return true;
                }

        });
	}

	private void updateAccountList() {
		accounts.removeAll();
		
		String def = settings.getString("defaultAccount", "N/A");
		
		for (String s : AllInOneV2.getAccounts().getKeys()) {
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
    		dialog = d;
    		break;
    	case MODIFY_ACCOUNT_DIALOG:
    		dialog = createModifyAccountV2Dialog();
    		break;
    	}
    	return dialog;
    }
    
    private Dialog createAddAccountDialog() {
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	LayoutInflater inflater = getLayoutInflater();
    	final View v = inflater.inflate(R.layout.addaccount, null);
    	b.setView(v);
    	b.setTitle("Add Account");
    	
    	b.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeDialog(ADD_ACCOUNT_DIALOG);
			}
		});
    	
    	b.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				verifyUser = ((TextView) v.findViewById(R.id.addaccUser)).getText().toString();
				verifyPass = ((TextView) v.findViewById(R.id.addaccPassword)).getText().toString();
				
				if (verifyUser.indexOf('@') == -1) {
					showDialog(VERIFY_ACCOUNT_DIALOG);
					new NetworkTask(SettingsAccount.this,
							NetDesc.VERIFY_ACCOUNT_S1, Method.GET,
							new HashMap<String, String>(), Session.ROOT, null)
							.execute();
				}
				else {
					Toast.makeText(SettingsAccount.this, 
							"Please use your username, not your email address.", 
							Toast.LENGTH_SHORT).show();
				}
			}
		});
    	
    	Dialog d = b.create();
    	return d;
    }
    
    private Dialog createModifyAccountV2Dialog() {
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	LayoutInflater inflater = getLayoutInflater();
    	final View v = inflater.inflate(R.layout.modifyaccountv2, null);
    	b.setView(v);
    	b.setTitle("Modify "  + clickedAccount.getTitle().toString());
    	
    	Button deleteAcc = (Button) v.findViewById(R.id.modaccDeleteAcc);
    	final CheckBox defaultAcc = (CheckBox) v.findViewById(R.id.modaccDefaultAccount);
    	final EditText sigContent = (EditText) v.findViewById(R.id.modaccSigContent);
    	final TextView sigCounter = (TextView) v.findViewById(R.id.modaccSigCounter);
    	
		if (clickedAccount.getTitle().toString().equals(settings.getString("defaultAccount", "N/A")))
			defaultAcc.setChecked(true);
		else
			defaultAcc.setChecked(false);
		
		defaultAcc.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					settings.edit().putString("defaultAccount", clickedAccount.getTitle().toString()).commit();
					Toast.makeText(SettingsAccount.this, "Default account saved.", Toast.LENGTH_SHORT).show();
				}
				else {
					settings.edit().putString("defaultAccount", "N/A").commit();
					settings.edit().putLong("notifsLastPost", 0).commit();
					Toast.makeText(SettingsAccount.this, "Default account removed.", Toast.LENGTH_SHORT).show();
				}
			}
		});
    	
    	deleteAcc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (clickedAccount.getTitle().toString().equals(settings.getString("defaultAccount", "N/A")))
					settings.edit().putString("defaultAccount", "N/A").commit();

				settings.edit().remove("customSig" + clickedAccount.getTitle().toString()).commit();
				
				AllInOneV2.getAccounts().removeValue(clickedAccount.getTitle().toString());
				accounts.removePreference(clickedAccount);
				dismissDialog(MODIFY_ACCOUNT_DIALOG);
				Toast.makeText(SettingsAccount.this, "Account removed.", Toast.LENGTH_SHORT).show();
			}
        });
    	
    	sigContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String escapedSig = StringEscapeUtils.escapeHtml4(sigContent.getText().toString());
				int length = escapedSig.length();
				int lines = 0;
				for(int i = 0; i < escapedSig.length(); i++) {
				    if(escapedSig.charAt(i) == '\n') lines++;
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
    	
    	b.setPositiveButton("Save Sig", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String escapedSig = StringEscapeUtils.escapeHtml4(sigContent.getText().toString());
				int length = escapedSig.length();
				int lines = 0;
				for(int i = 0; i < escapedSig.length(); i++) {
				    if(escapedSig.charAt(i) == '\n') lines++;
				}
				
				if (length < 161) {
					if (lines < 2) {
						settings.edit().putString("customSig" + clickedAccount.getTitle().toString(), sigContent.getText().toString()).commit();
						Toast.makeText(SettingsAccount.this, "Signature saved.", Toast.LENGTH_SHORT).show();
					}
					else
						Toast.makeText(SettingsAccount.this, "Signatures can only have 1 line break.", Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(SettingsAccount.this, "Signatures can only have a maximum of 160 characters.", Toast.LENGTH_SHORT).show();
			}
		});
    	
    	b.setNeutralButton("Clear Sig", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				settings.edit().putString("customSig" + clickedAccount.getTitle().toString(), "").commit();
				sigContent.setText("");
				Toast.makeText(SettingsAccount.this, "Signature cleared and saved.", Toast.LENGTH_SHORT).show();
			}
		});
    	
    	b.setNegativeButton("Close", null);
    	
    	Dialog d = b.create();
    	
    	d.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				updateAccountList();
				removeDialog(MODIFY_ACCOUNT_DIALOG);
			}
		});
    	
    	return d;
    }
    
    @Override
	public void handleNetworkResult(Response res, NetDesc desc) {
		if (res != null) {
			Document pRes = null;
			try {
				pRes = res.parse();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (desc == NetDesc.VERIFY_ACCOUNT_S1) {
				String loginKey = pRes.getElementsByAttributeValue("name",
						"key").attr("value");
				HashMap<String, String> loginData = new HashMap<String, String>();
				// "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
				loginData.put("EMAILADDR", verifyUser);
				loginData.put("PASSWORD", verifyPass);
				loginData.put("path", Session.ROOT);
				loginData.put("key", loginKey);
				new NetworkTask(SettingsAccount.this, NetDesc.VERIFY_ACCOUNT_S2,
						Method.POST, res.cookies(), Session.ROOT
								+ "/user/login.html", loginData).execute();
			}
			else if (desc == NetDesc.VERIFY_ACCOUNT_S2) {
				if (!pRes.getElementsContainingOwnText("Welcome, " + verifyUser).isEmpty()) {
					AllInOneV2.getAccounts().put(verifyUser, verifyPass);
		    		dismissDialog(VERIFY_ACCOUNT_DIALOG);
					removeDialog(ADD_ACCOUNT_DIALOG);
		    		Toast.makeText(this, "Verification succeeded.", Toast.LENGTH_SHORT).show();
				}
				else {
					dismissDialog(VERIFY_ACCOUNT_DIALOG);
					showDialog(ADD_ACCOUNT_DIALOG);
		    		Toast.makeText(this, "Verification failed. Check your username and password and try again.", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else {
			dismissDialog(VERIFY_ACCOUNT_DIALOG);
    		Toast.makeText(this, "Network connection failed. Check your network settings.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void preExecuteSetup(NetDesc desc) {}

	@Override
	public void postExecuteCleanup(NetDesc desc) {
		updateAccountList();
	}
}

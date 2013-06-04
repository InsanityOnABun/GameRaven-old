package com.ioabsoftware.gameraven;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.widget.NumberPicker;
import org.holoeverywhere.widget.Toast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.ioabsoftware.gameraven.R;

public class SettingsMain extends SherlockPreferenceActivity {
	
	public static final int MODIFY_SIG_DIALOG = 200;
	public static final int MODIFY_AMPSORT_DIALOG = 201;
	
	private int currentBackupVer = 2;
	final ArrayList<String> ACCEPTED_KEYS = new ArrayList<String>();
	
	private PendingIntent notifPendingIntent;
	
	SharedPreferences settings;
	
	protected void onCreate(Bundle savedInstanceState) {
		settings = AllInOneV2.getSettingsPref();
        
        if (AllInOneV2.getUsingLightTheme()) {
        	setTheme(R.style.MyThemes_LightTheme);
        }
		
		super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.settingsmain);

		Intent notifierIntent = new Intent(this, NotifierService.class);
		notifPendingIntent = PendingIntent.getService(this, 0, notifierIntent, 0);

        ACCEPTED_KEYS.add("notifsEnable");
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
        
        findPreference("donate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder db = new AlertDialog.Builder(SettingsMain.this);
				db.setTitle("Donate to Developer");
				db.setMessage("Thank you for your support! If you'd like to donate to the developer " +
						"of GameRaven, enter your desired amount below and click donate. Thanks again!");
				LayoutInflater inflater = getLayoutInflater();
				final View v = inflater.inflate(R.layout.donate, null);
				db.setView(v);
				final NumberPicker d = (NumberPicker) v.findViewById(R.id.donDollars);
				d.setMinValue(1);
				d.setMaxValue(100);
				d.setValue(1);
				d.setWrapSelectorWheel(false);
				
				final NumberPicker c = (NumberPicker) v.findViewById(R.id.donCents);
				c.setMinValue(0);
				c.setMaxValue(99);
				c.setValue(0);
				c.setWrapSelectorWheel(false);
				
				db.setPositiveButton("Donate", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String donateUrl = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=" +
								"MB2SP64VHE7F2&lc=US&item_name=GameRaven%2c%20from%20Insanity%20On%20A%20Bun" +
								"%20Software&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted" +
								"&amount=" + d.getValue() + "%2e" + c.getValue();
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(donateUrl));
						startActivity(i);
					}
				});
				
				db.setNegativeButton("Cancel", null);
				
				db.create().show();
				return false;
			}
		});
        
        findPreference("manageAccounts").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                	startActivity(new Intent(SettingsMain.this, SettingsAccount.class));
                    return true;
                }
        });
        
        findPreference("notifsEnable").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ((Boolean) newValue == true) {
					// enabling notifications
					if (settings.getString("defaultAccount", "N/A").equals("N/A")) {
						Toast.makeText(SettingsMain.this, "You have no default account set!", Toast.LENGTH_SHORT).show();
						return false;
					}
					else {
						enableNotifs(settings.getString("notifsFrequency", "60"));
					}
				}
				else {
					// disabling notifications
					disableNotifs();
				}
				return true;
			}
		});
        
        findPreference("notifsFrequency").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				disableNotifs();
				enableNotifs((String) newValue);
				return true;
			}
		});
        
        findPreference("customSig").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    showDialog(MODIFY_SIG_DIALOG);
                    return true;
                }
        });
        
        findPreference("ampSortOptionDialog").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    showDialog(MODIFY_AMPSORT_DIALOG);
                    return true;
                }
        });
        
        findPreference("manageHighlightedUsers").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	startActivity(new Intent(SettingsMain.this, SettingsHighlightedUsers.class));
            	return true;
            }
        });
        
        findPreference("resetAccentColor").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				settings.edit().putInt("accentColor", getResources().getColor(R.color.holo_blue)).commit();
				Toast.makeText(SettingsMain.this, "Accent color reset.", Toast.LENGTH_SHORT).show();
				finish();
				startActivity(getIntent());
				return false;
			}
		});
        
        findPreference("backupSettings").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                	AlertDialog.Builder bb = new AlertDialog.Builder(SettingsMain.this);
                	bb.setTitle("Backup Settings");
                	bb.setMessage("Are you sure you want to back up your settings? This will overwrite " +
                				  "any previous backup, and passwords are stored as plain text.");
                	bb.setPositiveButton("Yes", new OnClickListener() {
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
        
        findPreference("restoreSettings").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                	AlertDialog.Builder rb = new AlertDialog.Builder(SettingsMain.this);
                	rb.setTitle("Restore Settings");
                	rb.setMessage("Are you sure you want to restore your settings? This will wipe any previously added accounts.");
                	rb.setPositiveButton("Yes", new OnClickListener() {
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
        
        findPreference("aboutFeedback").setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                	startActivity(new Intent(SettingsMain.this, About.class));
                    return true;
                }
        });
	}

	private void enableNotifs(String freq) {
		long millis = 60000 * Integer.parseInt(freq);
		long firstAlarm = SystemClock.elapsedRealtime() + millis;
		((AlarmManager)getSystemService(Context.ALARM_SERVICE))
					.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstAlarm, millis, notifPendingIntent);
		settings.edit().putLong("notifsLastPost", 0).commit();
	}

	private void disableNotifs() {
		((AlarmManager)getSystemService(Context.ALARM_SERVICE)).cancel(notifPendingIntent);
	}
	
	private void backupSettings() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			File settingsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gameraven",
					"gameraven_settings");

			try {
				settingsFile.delete();
				settingsFile.createNewFile();
				
				BufferedWriter buf = new BufferedWriter(new FileWriter(
						settingsFile, true));
				
				buf.append("grBackupVer=" + currentBackupVer + "\n");
				
				buf.append("[ACCOUNTS]\n");
				for (String s : AllInOneV2.getAccounts().getKeys()) {
					
					buf.append(s + "\n");
					buf.append(AllInOneV2.getAccounts().getString(s) + "\n");
					
					buf.append("[CUSTOM_SIG]\n");
					buf.append(settings.getString("customSig" + s, "") + '\n');
					buf.append("[END_CUSTOM_SIG]\n");
				}
				buf.append("[END_ACCOUNTS]\n");
				
				buf.append("[GLOBAL_SIG]\n");
				buf.append(settings.getString("customSig", "") + '\n');
				buf.append("[END_GLOBAL_SIG]\n");
				
				buf.append("defaultAccount=" + settings.getString("defaultAccount", "N/A") + '\n');
				
				if (settings.getBoolean("notifsEnable", false))
					buf.append("notifsEnable=true\n");
				else
					buf.append("notifsEnable=false\n");

				buf.append("notifsFrequency=" + settings.getString("notifsFrequency", "60") + '\n');
				
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
				
				if (settings.getBoolean("useLightTheme", false))
					buf.append("useLightTheme=true\n");
				else
					buf.append("useLightTheme=false\n");
				
				if (settings.getBoolean("useWhiteAccentText", false))
					buf.append("useWhiteAccentText=true\n");
				else
					buf.append("useWhiteAccentText=false\n");
				
				buf.append("accentColor=" + settings.getInt("accentColor", getResources().getColor(R.color.holo_blue)) + '\n');
				
				if (settings.getBoolean("enableJS", true))
					buf.append("enableJS=true\n");
				else
					buf.append("enableJS=false\n");
				
				buf.append("ampSortOption=" + settings.getString("ampSortOption", "-1") + '\n');
				
				if (settings.getBoolean("confirmPostCancel", false))
					buf.append("confirmPostCancel=true\n");
				else
					buf.append("confirmPostCancel=false\n");
				
				if (settings.getBoolean("confirmPostSubmit", false))
					buf.append("confirmPostSubmit=true\n");
				else
					buf.append("confirmPostSubmit=false\n");
				
				buf.close();
				Toast.makeText(this, "Backup done.", Toast.LENGTH_SHORT).show();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			Log.e("writeToLog", "error writing to log, external storage is not writable");
			Toast.makeText(this, "Backup failed. External storage is most likely not accessible.", Toast.LENGTH_SHORT).show();
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
					Editor editor = settings.edit();
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
								}
								
								else if (line.equals("[GLOBAL_SIG]")) {
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
								}
								else
									Toast.makeText(this, "Line unhandled: " + line, Toast.LENGTH_LONG).show();
							}
							else if (line.contains("=")) {
								splitLine = line.split("=", 2);
								keys.add(splitLine[0]);
								values.add(splitLine[1]);
							}
							else
								Toast.makeText(this, "Line unhandled: " + line, Toast.LENGTH_LONG).show();
						}
					}
					
					br.close();
					
					// clear accs before adding in restored ones
					AllInOneV2.getAccounts().clear();
					
					for (int a = 0; a < users.size(); a++) {
						AllInOneV2.getAccounts().put(users.get(a), passwords.get(a));
					}
					
					for (int x = 0; x < keys.size(); x++) {
						String key = keys.get(x);
						String val = values.get(x);
						
						if (ACCEPTED_KEYS.contains(key) || key.startsWith("customSig")) {
							if (val.equals("true") || val.equals("false")) {
								editor.putBoolean(key, Boolean.parseBoolean(val));
							}
							else if (isInteger(val)) {
								if (key.equals("ampSortOption") || key.equals("notifsFrequency"))
									editor.putString(key, val);
								else
									editor.putInt(key, Integer.parseInt(val));
							}
							else
								editor.putString(key, val);
						}
						else
							Toast.makeText(this, "Key, Val pair not recognized: " + key + ", " + val, Toast.LENGTH_LONG).show();
					}
					
					editor.commit();
					
					Toast.makeText(this, "Restore done.", Toast.LENGTH_SHORT).show();
					finish();
					startActivity(getIntent());

				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, "Settings file is corrupt.", Toast.LENGTH_SHORT).show();
				}
			}
			else {
				Toast.makeText(this, "Settings file not found.", Toast.LENGTH_SHORT).show();
			}
		} 
		else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			Log.e("writeToLog", "error writing to log, external storage is not writable");
			Toast.makeText(this, "Restore failed. External storage is most likely not accessible.", Toast.LENGTH_SHORT).show();
		}
	}
	
	// creates dialogs
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	switch (id) {
    	case MODIFY_SIG_DIALOG:
    		dialog = createModifySigDialog();
    		break;
    	case MODIFY_AMPSORT_DIALOG:
    		dialog = createAMPSortDialog();
    		break;
    	}
    	return dialog;
    }
    
    private Dialog createModifySigDialog() {
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	LayoutInflater inflater = getLayoutInflater();
    	final View v = inflater.inflate(R.layout.modifysig, null);
    	b.setView(v);
    	b.setTitle("Modify Global Custom Signature");
    	
    	final EditText sigText = (EditText) v.findViewById(R.id.sigEditText);
    	final TextView sigCounter = (TextView) v.findViewById(R.id.sigCounter);
    	
    	sigText.setHint(AllInOneV2.defaultSig);
    	sigText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String escapedSig = StringEscapeUtils.escapeHtml4(sigText.getText().toString());
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
		sigText.setText(settings.getString("customSig", ""));
		
		b.setPositiveButton("Save Sig", null);
		b.setNeutralButton("Clear Sig", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				settings.edit().putString("customSig", "").commit();
				Toast.makeText(SettingsMain.this, "Signature cleared and saved.", Toast.LENGTH_SHORT).show();
			}
		});
		b.setNegativeButton("Cancel", null);

    	final AlertDialog d = b.create();
    	d.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		            	String escapedSig = StringEscapeUtils.escapeHtml4(sigText.getText().toString());
						int length = escapedSig.length();
						int lines = 0;
						for(int i = 0; i < escapedSig.length(); i++) {
						    if(escapedSig.charAt(i) == '\n') lines++;
						}
						
						if (length < 161) {
							if (lines < 2) {
								settings.edit().putString("customSig", sigText.getText().toString()).commit();
								Toast.makeText(SettingsMain.this, "Signature saved.", Toast.LENGTH_SHORT).show();
								d.dismiss();
							}
							else
								Toast.makeText(SettingsMain.this, "Signatures can only have 1 line break.", Toast.LENGTH_SHORT).show();
						}
						else
							Toast.makeText(SettingsMain.this, "Signatures can only have a maximum of 160 characters.", Toast.LENGTH_SHORT).show();
		            }
		        });
			}
		});
    	
    	d.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				removeDialog(MODIFY_SIG_DIALOG);
			}
		});
    	
    	return d;
    }
    
    private Dialog createAMPSortDialog() {
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle("AMP Sort Options");
    	final String[] vals = getResources().getStringArray(R.array.ampOptionValues);
    	String currVal = settings.getString("ampSortOption", "-1");
    	
    	int checkedItem = -1;
    	for (int x = 0; x < 4; x++) {
    		if (vals[x].equals(currVal))
    			checkedItem = x;
    	}
    	
    	b.setSingleChoiceItems(getResources().getStringArray(R.array.ampOptionKeys), checkedItem, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				settings.edit().putString("ampSortOption", vals[which]).commit();
				dialog.dismiss();
			}
		});
    	
    	b.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	
    	Dialog d = b.create();
    	d.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				removeDialog(MODIFY_AMPSORT_DIALOG);
			}
		});
    	
    	return d;
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

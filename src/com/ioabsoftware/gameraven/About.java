package com.ioabsoftware.gameraven;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.ioabsoftware.gameraven.R;

public class About extends SherlockActivity {

	public void onCreate(Bundle savedInstanceState) {
		if (AllInOneV2.getSettingsPref().getBoolean("useLightTheme", false)) {
        	setTheme(R.style.MyThemes_LightTheme);
        }
    	
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.about);
        
        try {
			((TextView) findViewById(R.id.abtBuildVer)).setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			((TextView) findViewById(R.id.abtBuildVer)).setText("Build version not set. Stupid developer.");
		}
	}
	
	public void bugReport(View view) {
		Intent send = new Intent(Intent.ACTION_SENDTO);
		String uriText;

		uriText = "mailto:ioabsoftware@gmail.com" + 
		          "?subject=GameRaven Bug Report";
		uriText = uriText.replace(" ", "%20");
		Uri uri = Uri.parse(uriText);

		send.setData(uri);
		startActivity(Intent.createChooser(send, "Send email..."));
	}
	
	public void featureRequest(View view) {
		Intent send = new Intent(Intent.ACTION_SENDTO);
		String uriText;

		uriText = "mailto:ioabsoftware@gmail.com" + 
		          "?subject=GameRaven Feature Request";
		uriText = uriText.replace(" ", "%20");
		Uri uri = Uri.parse(uriText);

		send.setData(uri);
		startActivity(Intent.createChooser(send, "Send email..."));
	}
	
	public void genFeedback(View view) {
		Intent send = new Intent(Intent.ACTION_SENDTO);
		String uriText;

		uriText = "mailto:ioabsoftware@gmail.com" + 
		          "?subject=GameRaven General Feedback";
		uriText = uriText.replace(" ", "%20");
		Uri uri = Uri.parse(uriText);

		send.setData(uri);
		startActivity(Intent.createChooser(send, "Send email..."));
	}
	
	public void viewBugs(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.evernote.com/shard/s252/sh/e4f3167a-32d4-4afd-92a2-1ff7a24f9046/4e062f39a5c13850a8f54ec9630f631f"));
		startActivity(browserIntent);
	}
	
	public void viewPrivacyPolicy(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.privacychoice.org/policy/mobile?policy=11f27c93a595b37228367eeafb872d7c"));
		startActivity(browserIntent);
	}
	
	public void updateCheck(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.evernote.com/shard/s252/sh/b680bb2b-64a1-426d-a98d-6cbfb846a883/75eebb4db64c6e1769dd2d0ace487a88"));
		startActivity(browserIntent);
	}
}

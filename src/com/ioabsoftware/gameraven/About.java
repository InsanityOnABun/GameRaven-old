package com.ioabsoftware.gameraven;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class About extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		if (AllInOneV2.getUsingLightTheme()) {
        	setTheme(R.style.MyThemes_LightTheme);
        }
    	
    	super.onCreate(savedInstanceState);
    	
    	setContentView(R.layout.about);
    	
    	Drawable aBarDrawable;
		if (AllInOneV2.getUsingLightTheme())
			aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_dark_holo);
		else
			aBarDrawable = getResources().getDrawable(R.drawable.ab_transparent_light_holo);
		
		aBarDrawable.setColorFilter(AllInOneV2.getAccentColor(), PorterDuff.Mode.SRC_ATOP);
		getActionBar().setBackgroundDrawable(aBarDrawable);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        
        try {
			((TextView) findViewById(R.id.abtBuildVer)).setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			((TextView) findViewById(R.id.abtBuildVer)).setText("Build version not set. Stupid developer.");
		}
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
	
	public void viewPrivacyPolicy(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.privacychoice.org/policy/mobile?policy=11f27c93a595b37228367eeafb872d7c"));
		startActivity(browserIntent);
	}
}

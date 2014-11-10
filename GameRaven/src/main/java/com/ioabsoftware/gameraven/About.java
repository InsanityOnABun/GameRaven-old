package com.ioabsoftware.gameraven;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ioabsoftware.gameraven.util.Theming;

public class About extends ActionBarActivity {

    public void onCreate(Bundle savedInstanceState) {
        if (Theming.usingLightTheme()) {
            setTheme(R.style.MyThemes_LightBase);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);

        Theming.colorOverscroll(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

package com.ioabsoftware.gameraven;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ioabsoftware.gameraven.util.Theming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class About extends ActionBarActivity {

    public void onCreate(Bundle savedInstanceState) {
        setTheme(Theming.theme());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Theming.colorPrimaryDark());
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);

        Theming.colorOverscroll(this);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.abtToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            String ver = "Version " + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName +
                    "\nBuild Number " + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
            ((TextView) findViewById(R.id.abtBuildVer)).setText(ver);
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
        try {
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.privacypolicy)));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line).append('\n');
            }
            br.close();

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Privacy Policy");
            b.setMessage(text.toString());
            b.setPositiveButton("OK", null);
            b.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

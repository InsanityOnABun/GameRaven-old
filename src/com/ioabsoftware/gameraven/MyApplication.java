package com.ioabsoftware.gameraven;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(
		formKey = "",
		formUri = "https://ioabsoftware.cloudant.com/acra-gameraven/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="somedfoldersessesetteade",
        formUriBasicAuthPassword="Kg4y3nMrROogBSvqV87ASVJM"
		)
public class MyApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();

        // trigger the initialization of ACRA
        ACRA.init(this);
    }
}

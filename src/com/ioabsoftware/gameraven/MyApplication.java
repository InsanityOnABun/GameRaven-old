package com.ioabsoftware.gameraven;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", formUri = "")
public class MyApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();

        // trigger the initialization of ACRA
        ACRA.init(this);
    }
}

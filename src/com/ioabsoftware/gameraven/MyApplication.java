package com.ioabsoftware.gameraven;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import static org.acra.ReportField.*;

import android.app.Application;

@ReportsCrashes(
	formKey = "",
	formUri = "https://ioabsoftware.cloudant.com/acra-gameraven/_design/acra-storage/_update/report",
	reportType = org.acra.sender.HttpSender.Type.JSON,
	httpMethod = org.acra.sender.HttpSender.Method.PUT,
	formUriBasicAuthLogin = "somedfoldersessesetteade",
	formUriBasicAuthPassword = "Kg4y3nMrROogBSvqV87ASVJM",
	
	customReportContent = {
			REPORT_ID,
			APP_VERSION_CODE,
			APP_VERSION_NAME,
			PACKAGE_NAME,
			FILE_PATH,
			ANDROID_VERSION,
			BUILD,
			TOTAL_MEM_SIZE,
			AVAILABLE_MEM_SIZE,
			CUSTOM_DATA,
			STACK_TRACE,
			INITIAL_CONFIGURATION,
			CRASH_CONFIGURATION,
			DISPLAY,
			USER_COMMENT,
			USER_EMAIL,
			USER_APP_START_DATE,
			USER_CRASH_DATE,
			LOGCAT,
			INSTALLATION_ID,
			DEVICE_FEATURES,
			ENVIRONMENT,
			SHARED_PREFERENCES,
			SETTINGS_SYSTEM,
			SETTINGS_SECURE,
			SETTINGS_GLOBAL
	},
	excludeMatchingSharedPreferencesKeys = {"secureSalt"},
	
	mode = ReportingInteractionMode.DIALOG,
	resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
	resDialogText = R.string.crash_dialog_text,
//	resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
	resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
	resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
	resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
	)
public class MyApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();

        // trigger the initialization of ACRA
        ACRA.init(this);
    }
}

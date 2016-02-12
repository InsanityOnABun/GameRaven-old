package com.ioabsoftware.gameraven;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BUILD;
import static org.acra.ReportField.CRASH_CONFIGURATION;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.DEVICE_FEATURES;
import static org.acra.ReportField.DISPLAY;
import static org.acra.ReportField.ENVIRONMENT;
import static org.acra.ReportField.FILE_PATH;
import static org.acra.ReportField.INITIAL_CONFIGURATION;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.SETTINGS_GLOBAL;
import static org.acra.ReportField.SETTINGS_SECURE;
import static org.acra.ReportField.SETTINGS_SYSTEM;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_CRASH_DATE;
import static org.acra.ReportField.USER_EMAIL;

@ReportsCrashes(
        formUri = "https://ioabsoftware.cloudant.com/acra-gameraven/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin = "foreagarvencensigrormese",
        formUriBasicAuthPassword = "ac784d49717b77ea927b89ff36a2850d8cb1435b",

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
        logcatFilterByPid = true,

        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
        resDialogText = R.string.crash_dialog_text,
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
        resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // trigger the initialization of ACRA, but only for release builds
        if (!BuildConfig.DEBUG)
            ACRA.init(this);
    }
}

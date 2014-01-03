package com.ioabsoftware.gameraven;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class NotifierStartupReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (prefs.getBoolean("notifsEnable", false)) {
			Intent notifierIntent = new Intent(context, NotifierService.class);
			PendingIntent notifPendingIntent = PendingIntent.getService(
					context, 0, notifierIntent, 0);
			long millis = 60000 * Integer.parseInt(prefs.getString(
					"notifsFrequency", "60"));
			long firstAlarm = SystemClock.elapsedRealtime() + millis;
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
					.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							firstAlarm, millis, notifPendingIntent);
		}
	}
}

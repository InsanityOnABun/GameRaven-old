package com.ioabsoftware.gameraven;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.Session;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;

public class NotifierService extends IntentService {
	
	public static final int NOTIF_ID = 1;
	public static final String NOTIF_TAG = "GR_NOTIF";
	
	
	public NotifierService() {
		super("GameRavenNotifierWorker");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("notif", "starting onhandleintent");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String username = prefs.getString("defaultAccount", "N/A");
		
	    if (!username.equals("N/A")) {
			HashMap<String, String> cookies = new HashMap<String, String>();
			String password = new AccountPreferences(getApplicationContext(), AllInOneV2.ACCOUNTS_PREFNAME, 
													 AllInOneV2.SALT, false).getString(username);;
			Log.d("notif", username);
			String basePath = Session.ROOT + "/boards";
			String loginPath = Session.ROOT + "/user/login.html";
			try {
				Response r = Jsoup.connect(basePath).method(Method.GET)
						.cookies(cookies).timeout(10000).execute();

				cookies.putAll(r.cookies());

				Log.d("notif", "first connection finished");
				Document pRes = r.parse();

				String loginKey = pRes.getElementsByAttributeValue("name",
						"key").attr("value");

				HashMap<String, String> loginData = new HashMap<String, String>();
				// "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
				loginData.put("EMAILADDR", username);
				loginData.put("PASSWORD", password);
				loginData.put("path", basePath);
				loginData.put("key", loginKey);

				Log.d("notif", username + ", " + loginPath
						+ ", " + loginKey);

				r = Jsoup.connect(loginPath).method(Method.POST)
						.cookies(cookies).data(loginData).timeout(10000)
						.execute();

				cookies.putAll(r.cookies());

				Log.d("notif", "second connection finished");

				r = Jsoup.connect(Session.ROOT + "/boards/myposts.php?lp=-1")
						.method(Method.GET).cookies(cookies).timeout(10000)
						.execute();

				Log.d("notif", "third connection finished");

				if (r.statusCode() != 401) {
					boolean amp = false, tt = false , pm = false;
					Log.d("notif", "status is good");
					pRes = r.parse();
					Log.d("notif", pRes.title());
					
					if (prefs.getBoolean("notifsAMPEnable", false)) {
						Element lPost = pRes.select("td.lastpost").first();
						if (lPost != null) {
							// 4/25 8:23PM
							// 1/24/2012
							String lTime = lPost.text();
							lTime = lTime.replace("Last:", "");
							Log.d("notif", "time is " + lTime);
							Date newDate;
							if (lTime.contains("AM") || lTime.contains("PM"))
								newDate = new SimpleDateFormat(
										"MM'/'dd hh':'mmaa", Locale.US)
										.parse(lTime);
							else
								newDate = new SimpleDateFormat(
										"MM'/'dd'/'yyyy", Locale.US)
										.parse(lTime);

							long newTime = newDate.getTime();
							long oldTime = prefs.getLong("notifsLastPost", 0);
							if (newTime > oldTime) {
								amp = true;
								Log.d("notif", "time is newer");
								prefs.edit().putLong("notifsLastPost", newTime)
										.commit();
							}
						}
					}
					
					if (prefs.getBoolean("notifsPMEnable", false)) {
						Element pmInboxLink = pRes.select("a[href=/pm/]")
								.first();
						if (pmInboxLink != null) {
							if (!pmInboxLink.text().equals("Inbox")) {
								pm = true;
							}
						}
					}
					
					if (prefs.getBoolean("notifsTTEnable", false)) {
						Element trackedLink = pRes.select(
								"a[href=/boards/tracked]").first();
						if (trackedLink != null) {
							if (!trackedLink.text().equals("Topics")) {
								tt = true;
							}
						}
					}
					
					if (amp || pm || tt) {
						StringBuilder notifText = new StringBuilder("New ");
						
						if (pm) {
							if (amp || tt)
								notifText.append("PM(s), ");
							else
								notifText.append("PM(s)");
						}
						if (amp || tt) {
							notifText.append("post(s) in ");
							if (amp && tt)
								notifText.append("AMP list & tracked topics");
							else if (amp)
								notifText.append("AMP list");
							else if (tt)
								notifText.append("tracked topics");
						}
						
						notifText.append(" found");
						
						NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(
								this)
								.setSmallIcon(R.drawable.ic_notif_small)
								.setContentTitle("GameRaven")
								.setContentText(notifText.toString());
						Intent notifIntent = new Intent(this, AllInOneV2.class);
						PendingIntent pendingNotif = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_ONE_SHOT);
						notifBuilder.setContentIntent(pendingNotif);
						notifBuilder.setAutoCancel(true);
						notifBuilder.setDefaults(Notification.DEFAULT_ALL);
						NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						// mId allows you to update the notification later on.
						notifManager.notify(NOTIF_TAG, NOTIF_ID, notifBuilder.build());
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Toast.makeText(this,
						"There was an error checking for new messages",
						Toast.LENGTH_SHORT).show();
				Log.d("notif", "exception raised in notifierservice");
				e.printStackTrace();
			}
		}
	}
	
	public static void dismissNotif(Context c) {
		NotificationManager notifManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.cancel(NOTIF_TAG, NOTIF_ID);
	}

}

package com.ioabsoftware.gameraven;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ioabsoftware.gameraven.networking.Session;
import com.ioabsoftware.gameraven.prefs.HeaderSettings;
import com.ioabsoftware.gameraven.util.AccountManager;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class NotifierService extends IntentService {

    public static final String NOTIF_TAG = "GR_NOTIF";
    public static final int NOTIF_ID = 1;


    public NotifierService() {
        super("GameRavenNotifierWorker");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("notif", "notif service starting");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String username = prefs.getString("defaultAccount", HeaderSettings.NO_DEFAULT_ACCOUNT);

        // double check notifications are enabled
        // service does nothing if there is no default account set or there is no generated salt
        if (prefs.getBoolean("notifsEnable", false) && !username.equals(HeaderSettings.NO_DEFAULT_ACCOUNT) && prefs.getString("secureSalt", null) != null) {
            Log.d("notif", "doing the thing");
            try {
                long rightNow = System.currentTimeMillis();

                HashMap<String, String> cookies = new HashMap<String, String>();
                String password = AccountManager.getPassword(getApplicationContext(), username);

                String basePath = Session.ROOT + "/user/notifications";
                String loginPath = Session.ROOT + "/user/login";

                Response r = Jsoup.connect(loginPath).method(Method.GET)
                        .cookies(cookies).timeout(10000).execute();

                cookies.putAll(r.cookies());

                // first connection finished
                Document pRes = r.parse();

                String loginKey = pRes.getElementsByAttributeValue("name",
                        "key").attr("value");

                HashMap<String, String> loginData = new HashMap<String, String>();
                // "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
                loginData.put("EMAILADDR", username);
                loginData.put("PASSWORD", password);
                loginData.put("path", basePath);
                loginData.put("key", loginKey);

                r = Jsoup.connect(loginPath).method(Method.POST)
                        .cookies(cookies).data(loginData).timeout(10000)
                        .execute();

                cookies.putAll(r.cookies());

                // second connection finished

                r = Jsoup.connect(basePath).method(Method.GET)
                        .cookies(cookies).data(loginData).timeout(10000)
                        .execute();

                // third connection finished

                cookies.putAll(r.cookies());

                if (r.statusCode() != 401) {
                    pRes = r.parse();

                    NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // NOTIF PAGE PROCESS START

                    Element tbody = pRes.getElementsByTag("tbody").first();

                    if (tbody != null) {
                        Element latest = tbody.getElementsByTag("tr").first();
                        if (!latest.child(2).text().equals("Read")) {
                            long millis = 0;
                            int multiplier = 1000;
                            String fuzzyTimestamp = latest.child(1).text();
                            if (fuzzyTimestamp.contains("second")) {
                                multiplier *= 1;
                            } else if (fuzzyTimestamp.contains("minute")) {
                                multiplier *= 60; // 1* 60
                            } else if (fuzzyTimestamp.contains("hour")) {
                                multiplier *= 3600; // 1 * 60 * 60
                            } else if (fuzzyTimestamp.contains("day")) {
                                multiplier *= 86400; // 1 * 60 * 60 * 24
                            } else if (fuzzyTimestamp.contains("week")) {
                                multiplier *= 604800; //1 * 60 * 60 * 24 * 7
                            }

                            int firstSpace = fuzzyTimestamp.indexOf(' ');
                            millis = Long.valueOf(fuzzyTimestamp.substring(0, firstSpace)) * multiplier;

                            long notifTime = rightNow - millis;
                            long lastCheck = prefs.getLong("notifsLastCheck", 0);

                            if (notifTime > lastCheck) {
                                Notification.Builder notifBuilder = new Notification.Builder(this)
                                        .setSmallIcon(R.drawable.ic_notif_small)
                                        .setContentTitle("GameRaven")
                                        .setContentText("You have new notification(s)");
                                Intent notifIntent = new Intent(this, AllInOneV2.class);
                                PendingIntent pendingNotif = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_ONE_SHOT);
                                notifBuilder.setContentIntent(pendingNotif);
                                notifBuilder.setAutoCancel(true);
                                notifBuilder.setDefaults(Notification.DEFAULT_ALL);

                                notifManager.notify(NOTIF_TAG, NOTIF_ID, notifBuilder.getNotification());
                            }
                        }
                    }

                    prefs.edit().putLong("notifsLastCheck", rightNow).apply();
                    // NOTIF PAGE PROCESS END
                }

            } catch (Exception e) {
                Log.d("notif", "exception raised in notifierservice");
                e.printStackTrace();
            }
        }
    }

    public static void notifDismiss(Context c) {
        ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIF_TAG, NOTIF_ID);
    }
}

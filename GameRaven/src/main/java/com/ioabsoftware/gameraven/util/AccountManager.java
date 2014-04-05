package com.ioabsoftware.gameraven.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public final class AccountManager {
    protected static final String ACCOUNTS_PREFNAME = "com.ioabsoftware.DroidFAQs.Accounts";
    protected static String secureSalt;

    /**
     * list of accounts (username, password)
     */
    private static SecurePreferences accounts;

    public static void init(Context c) {
        if (accounts == null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
            if (settings.contains("secureSalt"))
                secureSalt = settings.getString("secureSalt", null);
            else {
                secureSalt = UUID.randomUUID().toString();
                settings.edit().putString("secureSalt", secureSalt).commit();
            }

            accounts = new SecurePreferences(c, ACCOUNTS_PREFNAME, secureSalt, false);
        }
    }

    public static void addUser(Context c, String name, String pass) {
        init(c);
        accounts.put(name, pass);
    }

    public static void removeUser(Context c, String user) {
        init(c);
        accounts.removeValue(user);
    }

    public static boolean containsUser(Context c, String user) {
        init(c);
        return accounts.containsKey(user);
    }

    public static String[] getUsernames(Context c) {
        init(c);
        return accounts.getKeys();
    }

    public static String getPassword(Context c, String username) {
        init(c);
        return accounts.getString(username);
    }

    public static void clearAccounts(Context c) {
        init(c);
        accounts.clear();
    }
}

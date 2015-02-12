package net.labhackercd.edemocracia.data.api;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesCredentialStorage {
    private static final String KEY = "user";
    private static final String SHARED_PREFERENCES =
            SharedPreferencesCredentialStorage.class.getCanonicalName();

    private String credentials;
    private final Context context;

    public SharedPreferencesCredentialStorage(Context context) {
        this.context = context.getApplicationContext();
    }

    public void save(String credentials) {
        this.credentials = credentials;

        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(KEY, credentials);
        editor.apply();
    }

    public String load() {
        if (credentials == null) {
            SharedPreferences prefs = getSharedPreferences();
            credentials = prefs.getString(KEY, "null");
        }
        return credentials;
    }

    public void clear() {
        credentials = null;

        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(KEY);
        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SHARED_PREFERENCES, 0);
    }
}

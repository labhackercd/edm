package net.labhackercd.edemocracia.account;

import android.content.SharedPreferences;

/**
 * Storing credentials in the SharedPreferences is not a good practice,
 * but what can one do when there's not enough time or knowledge?
 */
public class SharedPreferencesCredentialStore implements CredentialStore {
    private static final String KEY = SharedPreferencesCredentialStore.class.getCanonicalName();

    private final SharedPreferences sharedPreferences;
    private String credentials;

    public SharedPreferencesCredentialStore(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String get() {
        if (credentials == null) {
            credentials = sharedPreferences.getString(KEY, "null");
        }
        return credentials;
    }

    public void set(String credentials) {
        this.credentials = credentials;

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (credentials == null) {
            editor.remove(KEY);
        } else {
            editor.putString(KEY, credentials);
        }

        editor.apply();
    }

    public void clear() {
        set(null);
    }
}

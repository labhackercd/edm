package net.labhackercd.edemocracia.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Persists {@link EDMAuthentication} through {@link android.content.SharedPreferences}.
 */
public class CredentialsStorage {
    private static final String FIELD = "credentials";

    public static void store(Context context, EDMAuthentication credentials) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        String value = getGson().toJson(credentials);

        editor.putString(FIELD, value);

        editor.apply();
    }

    public static EDMAuthentication load(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);

        return getGson().fromJson(prefs.getString(FIELD, "null"), EDMAuthentication.class);
    }

    private static Gson getGson() {
        return new Gson();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(CredentialsStorage.class.getCanonicalName(), 0);
    }
}
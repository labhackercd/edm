package net.labhackercd.edemocracia.data.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.edemocracia.data.api.model.User;

/**
 * Manages EDMSessions for a context.
 */
public class SessionManager {

    private static final String USER_KEY = "user";
    private static final String CREDENTIALS_KEY = "credentials";
    private static final String SHARED_PREFERENCES = "net.labhackercd.EDMApplication";

    private final Context context;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        this.context = context;
    }

    public void save(EDMSession session) {
        if (session == null) {
            clear();
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences().edit();

        editor.putString(CREDENTIALS_KEY, gson.toJson(session.getAuthentication()));
        editor.putString(USER_KEY, gson.toJson(session.getUser()));

        editor.apply();
    }

    @Nullable
    public EDMSession load() {
        SharedPreferences prefs = getSharedPreferences();

        String jsonUser = prefs.getString(USER_KEY, "null");
        String jsonCredentials = prefs.getString(CREDENTIALS_KEY, "null");

        User user = gson.fromJson(jsonUser, User.class);
        BasicAuthentication credentials = gson.fromJson(jsonCredentials, BasicAuthentication.class);

        return new EDMSession(credentials, user);
    }

    public void clear() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        editor.remove(USER_KEY);
        editor.remove(CREDENTIALS_KEY);

        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SHARED_PREFERENCES, 0);
    }
}

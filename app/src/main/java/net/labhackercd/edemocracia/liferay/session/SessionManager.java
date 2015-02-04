package net.labhackercd.edemocracia.liferay.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;

/**
 * Manages EDMSessions for a context.
 */
public class SessionManager {

    private static final String SHARED_PREFERENCES = "net.labhackercd.EDMApplication";
    private static final String CREDENTIALS_KEY = "credentials";
    private static final String COMPANY_ID_KEY = "compnayId";

    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
    }

    public void save(EDMSession session) {
        if (session == null) {
            clear();
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences().edit();

        editor.putString(CREDENTIALS_KEY, new Gson().toJson(session.getAuthentication()));
        editor.putLong(COMPANY_ID_KEY, session.getCompanyId());

        editor.apply();
    }

    @Nullable
    public EDMSession load() {
        SharedPreferences prefs = getSharedPreferences();

        String json = prefs.getString(CREDENTIALS_KEY, "null");

        long companyId = prefs.getLong(COMPANY_ID_KEY, -1);
        BasicAuthentication credentials = new Gson().fromJson(json, BasicAuthentication.class);

        return new EDMSession(credentials, companyId);
    }

    public void clear() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        editor.remove(CREDENTIALS_KEY);
        editor.remove(COMPANY_ID_KEY);

        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SHARED_PREFERENCES, 0);
    }
}

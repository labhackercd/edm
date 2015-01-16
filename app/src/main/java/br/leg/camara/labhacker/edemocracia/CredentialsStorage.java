package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.leg.camara.labhacker.edemocracia.liferay.auth.CookieCredentials;

/**
 * Persists {@link CookieCredentials} through {@link android.content.SharedPreferences}.
 */
public class CredentialsStorage {
    private static final String KEY = "credentials";

    public static CookieCredentials load(Context context) {
        Gson gson = new Gson();

        Set<String> jsonValues = getSharedPreferences(context).getStringSet(KEY, null);

        if (jsonValues == null) {
            return null;
        }

        List<HttpCookie> cookies = new ArrayList<>();

        for (String value : jsonValues) {
            HttpCookie cookie = gson.fromJson(value, HttpCookie.class);
            cookies.add(cookie);
        }

        return new CookieCredentials(cookies);
    }

    public static int store(Context context, CookieCredentials credentials) {
        Gson gson = new Gson();

        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        List<HttpCookie> cookies;
        if (credentials != null) {
            cookies = credentials.getCookies();
        } else {
            cookies = new ArrayList<>();
        }

        Set<String> jsonValues = new HashSet<>();

        for (HttpCookie cookie : cookies) {
            jsonValues.add(gson.toJson(cookie));
        }

        editor.putStringSet(KEY, jsonValues);

        editor.apply();

        return jsonValues.size();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(CredentialsStorage.class.getCanonicalName(), 0);
    }
}
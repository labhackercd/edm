package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.leg.camara.labhacker.edemocracia.liferay.CookieCredentials;

/**
 * Helper class for loading and storing cookie based credentials on SharedPreferences.
 */
public class PersistentCredentials  {
    private static final String KEY = "credentials";

    public static CookieCredentials load(Context context) {
        Gson gson = new Gson();

        SharedPreferences settings = getSharedPreferences(context);
        Set<String> jsonCookies = settings.getStringSet(KEY, new HashSet<String>());
        List<HttpCookie> cookies = new ArrayList<>();

        if (jsonCookies.size() > 0) {
            for (String item : jsonCookies) {
                HttpCookie cookie = gson.fromJson(item, HttpCookie.class);
                cookies.add(cookie);
            }
        }

        return new CookieCredentials(cookies);
    }

    public static int store(Context context, CookieCredentials credentials) {
        Gson gson = new Gson();

        SharedPreferences settings = getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        Set<String> jsonCookies = new HashSet<>();

        for (HttpCookie cookie : credentials.getCookies()) {
            jsonCookies.add(gson.toJson(cookie));
        }

        editor.putStringSet(KEY, jsonCookies);

        editor.commit();

        return jsonCookies.size();
    }

    private static final SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PersistentCredentials.class.getCanonicalName(), 0);
    }
}

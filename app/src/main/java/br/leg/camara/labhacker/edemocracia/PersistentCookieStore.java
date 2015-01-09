package br.leg.camara.labhacker.edemocracia;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores cookies in `SharedPreferences`.
 */
public class PersistentCookieStore implements CookieStore {
    private CookieStore store;
    private Context context;
    private Gson gson;

    public PersistentCookieStore(Context context) {
        this.context = context;
        gson = new Gson();

        // Get the default in memory store and if there are any cookies in it, transfer
        // them to our persistent store.
        store = (new CookieManager()).getCookieStore();

        // Load stored cookies
        SharedPreferences settings = getSharedPreferences(this.context);
        Set<String> jsonCookies = settings.getStringSet("cookies", new HashSet<String>());

        if (jsonCookies.size() > 0) {
            // If there are any cookies stored, load them all into the in memory storage
            //store.removeAll();
            for (String item : jsonCookies) {
                HttpCookie cookie = gson.fromJson(item, HttpCookie.class);
                store.add(URI.create(cookie.getDomain()), cookie);
            }
        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        // Add the cookie to our in memory cookie store
        store.add(URI.create(cookie.getDomain()), cookie);

        // Then sync it
        persistCookies();
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return store.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return store.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return store.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        boolean success = store.remove(uri, cookie);
        if (success) {
            persistCookies();
        }
        return success;
    }

    @Override
    public boolean removeAll() {
        boolean success = store.removeAll();
        if (success) {
            persistCookies();
        }
        return success;
    }

    private int persistCookies() {
        SharedPreferences settings = getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        List<HttpCookie> storeCookies = store.getCookies();
        Set<String> jsonCookies = new HashSet<>(storeCookies.size());

        for (HttpCookie cookie : storeCookies) {
            jsonCookies.add(gson.toJson(cookie));
        }

        editor.putStringSet("cookies", jsonCookies);
        editor.commit();

        return jsonCookies.size();
    }

    private static final SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PersistentCookieStore.class.getCanonicalName(), 0);
    }
}

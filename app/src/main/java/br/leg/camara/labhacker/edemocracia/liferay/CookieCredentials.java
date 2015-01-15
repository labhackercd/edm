package br.leg.camara.labhacker.edemocracia.liferay;

import android.util.Log;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class CookieCredentials implements Session.Middleware {
    private CookieManager cookieManager;

    public CookieCredentials(List<HttpCookie> cookies) {
        cookieManager = new CookieManager();
        for (HttpCookie cookie : cookies) {
            cookieManager.getCookieStore().add(URI.create(cookie.getDomain()), cookie);
        }
    }

    public static CookieCredentials fromCookieStore(CookieStore store) {
        return new CookieCredentials(store.getCookies());
    }

    public List<HttpCookie> getCookies() {
        return cookieManager.getCookieStore().getCookies();
    }

    public void prepareRequest(HttpURLConnection request, Session session) {
        for (HttpCookie cookie : getCookies()) {
            request.addRequestProperty("Cookie", cookie.toString());
        }
    }

    public void processResponse(HttpURLConnection response, Session session) throws IOException {
        try {
            cookieManager.put(response.getURL().toURI(), response.getHeaderFields());
        } catch (URISyntaxException e) {
            Log.w(getClass().getSimpleName(), "Invalid response URI. Ignoring cookies.");
        }
    }
}

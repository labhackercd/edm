package br.leg.camara.labhacker.edemocracia.liferay;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class CookieCredentials {
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

    public void authenticate(HttpURLConnection connection) throws URISyntaxException, IOException {
        for (HttpCookie cookie : getCookies()) {
            connection.addRequestProperty("Cookie", cookie.toString());
        }

        // TODO FIXME This should really be placed somewhere else, like *processResponse* or something
        cookieManager.put(connection.getURL().toURI(), connection.getHeaderFields());
    }
}

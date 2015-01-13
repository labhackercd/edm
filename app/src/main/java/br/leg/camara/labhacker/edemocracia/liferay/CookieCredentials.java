package br.leg.camara.labhacker.edemocracia.liferay;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class CookieCredentials {
    private List<HttpCookie> cookies;

    public CookieCredentials(List<HttpCookie> cookies) {
        this.cookies = cookies;
    }

    public static CookieCredentials fromCookieStore(CookieStore store) {
        return new CookieCredentials(new ArrayList<>(store.getCookies()));
    }

    public List<HttpCookie> getCookies() {
        return cookies;
    }

    public void authenticate(HttpURLConnection connection) {
        for (HttpCookie cookie : cookies) {
            connection.addRequestProperty("Cookie", cookie.toString());
        }
    }
}

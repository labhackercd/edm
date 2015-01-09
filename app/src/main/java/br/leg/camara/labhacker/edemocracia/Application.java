package br.leg.camara.labhacker.edemocracia;

import android.content.SharedPreferences;

import java.net.CookieStore;

import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;

public class Application extends android.app.Application {
    private LiferayClient liferayClient;
    private PersistentCookieStore cookieStore;

    public CookieStore getCookieStore() {
        if (cookieStore == null) {
            cookieStore = new PersistentCookieStore(getApplicationContext());
        }
        return cookieStore;
    }

    public LiferayClient getLiferayClient() {
        if (this.liferayClient == null) {
            SharedPreferences settings = getApplicationContext().getSharedPreferences("AuthTokenFile", 0);
            String token = settings.getString("authToken", null);
            liferayClient = new LiferayClient(getCookieStore(), token);
        }
        return liferayClient;
    }
}

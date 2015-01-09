package br.leg.camara.labhacker.edemocracia;

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
            liferayClient = new LiferayClient(getCookieStore());
        }
        return liferayClient;
    }
}

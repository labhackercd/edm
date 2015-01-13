package br.leg.camara.labhacker.edemocracia;

import java.net.MalformedURLException;
import java.net.URL;

import br.leg.camara.labhacker.edemocracia.liferay.CookieCredentials;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;

public class Application extends android.app.Application {
    private LiferayClient liferayClient = null;
    private CookieCredentials credentials = null;

    public static final URL SERVICE_URL;
    public static final URL SERVICE_LOGIN_URL;
    public static final int DEFAULT_COMPANY_ID = 10131;

    static {
        try {
            SERVICE_URL = new URL("http://edemocracia.camara.gov.br/api/jsonws");
            SERVICE_LOGIN_URL = new URL("http://edemocracia.camara.gov.br/cadastro");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public CookieCredentials getCredentials() {
        if (credentials == null) {
            credentials = PersistentCredentials.load(getApplicationContext());
        }
        return credentials;
    }

    public void setCredentials(CookieCredentials credentials) {
        int n = PersistentCredentials.store(getApplicationContext(), credentials);

        this.credentials = credentials;
        this.liferayClient = null;
    }

    public LiferayClient getLiferayClient() {
        if (liferayClient == null) {
            liferayClient = new LiferayClient(SERVICE_URL, getCredentials());
        }
        return liferayClient;
    }
}

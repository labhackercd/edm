package br.leg.camara.labhacker.edemocracia;

import android.app.Application;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

import br.leg.camara.labhacker.edemocracia.liferay.auth.CookieAuthenticator;
import br.leg.camara.labhacker.edemocracia.liferay.auth.CookieCredentials;
import br.leg.camara.labhacker.edemocracia.liferay.SessionImpl;
import br.leg.camara.labhacker.edemocracia.liferay.Session;


/**
 * Provides Session objects for Applications.
 *
 * Create sessions using createSession. Reuse created sessions through getSession.
 */
public class SessionProvider {

    public static final int DEFAULT_COMPANY_ID = 10131;

    private static final URL SERVICE_URL;
    private static final URL SERVICE_LOGIN_URL;

    static {
        try {
            SERVICE_URL = new URL("http://edemocracia.camara.gov.br/api/jsonws/invoke");
            SERVICE_LOGIN_URL = new URL("http://edemocracia.camara.gov.br/cadastro");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<Application, Session> sessions = new WeakHashMap<>();

    public static Session getSession(Application application) {
        if (!sessions.containsKey(application)) {
            CookieCredentials credentials = CredentialsStorage.load(application.getApplicationContext());
            if (credentials != null) {
                return setCredentials(application, credentials, false);
            }
        }
        return sessions.get(application);
    }

    public static Session createSession(Application application, String username, String password) throws IOException {
        CookieCredentials credentials = CookieAuthenticator.authenticate(SERVICE_LOGIN_URL, username, password);

        return setCredentials(application, credentials);
    }

    private static Session setCredentials(Application application, CookieCredentials cookieCredentials) {
        return setCredentials(application, cookieCredentials, true);
    }

    private static Session setCredentials(Application application, CookieCredentials credentials, boolean store) {
        if (store) {
            CredentialsStorage.store(application.getApplicationContext(), credentials);
        }

        Session session = null;

        if (credentials != null) {
            session = new SessionImpl(SERVICE_URL, credentials);
        }

        sessions.put(application, session);


        return session;
    }
}
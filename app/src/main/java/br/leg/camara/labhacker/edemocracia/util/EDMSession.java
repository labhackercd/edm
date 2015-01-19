package br.leg.camara.labhacker.edemocracia.util;

import android.content.Context;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.SessionImpl;
import com.liferay.mobile.android.v62.group.GroupService;

import java.lang.reflect.Field;

public class EDMSession extends SessionImpl {
    private static final String SERVICE_URL = "http://edemocracia.camara.gov.br";
    private static ContextBinder<EDMSession> binder = new ContextBinder<>();
    private static boolean monkeyPatched = false;

    public EDMSession() {
        super(SERVICE_URL);
        monkeypatchServicePath();
    }

    public EDMSession(Authentication credentials) {
        super(SERVICE_URL, credentials);
        monkeypatchServicePath();
    }

    public EDMSession bind(Context applicationContext) {
        CredentialsStorage.store(applicationContext, getEDMAuthentication());
        return binder.put(applicationContext, this);
    }

    public static EDMSession get(Context applicationContext) {
        EDMSession session = binder.get(applicationContext, null);
        if (session == null) {
            Authentication credentials = CredentialsStorage.load(applicationContext);
            if (credentials != null) {
                session = new EDMSession(credentials);
            }
        }
        return session;
    }

    public long getCompanyId() {
        EDMAuthentication credentials = getEDMAuthentication();
        if (credentials != null) {
            return credentials.getCompanyId();
        } else {
            return -1;
        }
    }

    public boolean isAuthenticated() throws Exception {
        return this.getAuthentication() != null
                && this.getCompanyId() >= 0
                && new GroupService(this).getUserSites().length() > 0;
    }

    private EDMAuthentication getEDMAuthentication() {
        try {
            return (EDMAuthentication) super.getAuthentication();
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * XXX In Liferay 6.1, Basic HTTP authentication only works at this /api/secure/jsonws path.
     * liferay-mobile-sdk made the PATH private static, so we monkey-f*cking-patch it!!!
     */
    public static void monkeypatchServicePath() {
        if (!monkeyPatched) {
            try {
                Field wspath = HttpUtil.class.getDeclaredField("_JSONWS_PATH");
                wspath.setAccessible(true);
                wspath.set(null, "api/secure/jsonws");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            monkeyPatched = true;
        }
    }
}

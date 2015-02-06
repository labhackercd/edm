package net.labhackercd.edemocracia.liferay.session;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.SessionImpl;

import net.labhackercd.edemocracia.liferay.exception.AuthorizationException;
import net.labhackercd.edemocracia.liferay.exception.NotFoundException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class EDMSession extends SessionImpl {
    public static final String SERVICE_URL = "https://edemocracia.camara.gov.br";

    private static boolean monkeyPatched = false;

    private long companyId;

    public EDMSession() {
        super(SERVICE_URL);
        monkeyPatchServicePath();
    }

    public EDMSession(Authentication credentials) {
        super(SERVICE_URL, credentials);
        monkeyPatchServicePath();
    }

    public EDMSession(Authentication credentials, long companyId) {
        super(SERVICE_URL, credentials);
        this.companyId = companyId;
        monkeyPatchServicePath();
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    @Override
    public JSONArray invoke(JSONObject command) throws Exception {
        try {
            return super.invoke(command);
        } catch (ServerException e) {
            throw tryAndSpecializeException(e);
        }
    }

    protected Exception tryAndSpecializeException(Exception e) {
        String err = e.getMessage().toLowerCase();
        if (err.matches("no *such") || err.matches("no *\\w+ *exists")) {
            e = new NotFoundException(e);
        } else if (err.matches("please *sign.*") || err.matches("authenticated *access.*")
                || err.matches("authentication failed.*")) {
            e = new AuthorizationException(e);
        }
        return e;
    }

    /**
     * XXX In Liferay 6.1, Basic HTTP authentication only works at this /api/secure/jsonws path.
     * liferay-mobile-sdk made the PATH private static, so we monkey-f*cking-patch it!!!
     */
    private static void monkeyPatchServicePath() {
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

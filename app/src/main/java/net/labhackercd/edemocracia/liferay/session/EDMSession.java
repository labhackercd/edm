package net.labhackercd.edemocracia.liferay.session;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.exception.ServerException;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.SessionImpl;

import net.labhackercd.edemocracia.content.User;
import net.labhackercd.edemocracia.liferay.exception.AuthorizationException;
import net.labhackercd.edemocracia.liferay.exception.NotFoundException;
import net.labhackercd.edemocracia.liferay.exception.PrincipalException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class EDMSession extends SessionImpl {
    public static final String SERVICE_URL = "https://edemocracia.camara.gov.br";
    private static boolean monkeyPatched = false;

    private User user;

    public EDMSession() {
        super(SERVICE_URL);
        monkeyPatchServicePath();
    }

    public EDMSession(Authentication credentials) {
        super(SERVICE_URL, credentials);
        monkeyPatchServicePath();
    }

    public EDMSession(Authentication credentials, User user) {
        super(SERVICE_URL, credentials);
        this.user = user;
        monkeyPatchServicePath();
    }

    public long getCompanyId() {
        return user == null ? 0 : user.getCompanyId();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        String err = e.getMessage().toLowerCase().trim();
        if (err.matches(".*principal *exception")) {
            e = new PrincipalException(e);
        } else if (err.matches(".*no *such") || err.matches(".*no *\\w+ *exists")) {
            e = new NotFoundException(e);
        } else if (err.matches(".*please *sign") || err.matches(".*authenticated *access")
                || err.matches(".*authentication failed")) {
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

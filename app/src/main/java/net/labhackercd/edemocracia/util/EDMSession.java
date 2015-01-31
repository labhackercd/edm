package net.labhackercd.edemocracia.util;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.http.HttpUtil;
import com.liferay.mobile.android.service.SessionImpl;
import com.liferay.mobile.android.v62.group.GroupService;

import java.lang.reflect.Field;

public class EDMSession extends SessionImpl {
    private static final String SERVICE_URL = "http://edemocracia.camara.gov.br";
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

    public boolean isAuthenticated() throws Exception {
        return this.getAuthentication() != null
                && this.getCompanyId() >= 0
                && new GroupService(this).getUserSites().length() > 0;
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

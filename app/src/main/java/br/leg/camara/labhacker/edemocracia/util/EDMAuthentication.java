package br.leg.camara.labhacker.edemocracia.util;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;

public class EDMAuthentication extends BasicAuthentication {
    private final long companyId;

    public EDMAuthentication(String username, String password, long companyId) {
        super(username, password);
        this.companyId = companyId;
    }

    public long getCompanyId() {
        return this.companyId;
    }
}

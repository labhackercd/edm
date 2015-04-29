package net.labhackercd.nhegatu.data.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.nhegatu.account.AccountUtils;
import net.labhackercd.nhegatu.data.api.client.Endpoint;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(complete = false, library = true)
@SuppressWarnings("UnusedDeclaration")
public class ApiModule {
    private static final String PRODUCTION_PORTAL_URL = "https://edemocracia.camara.gov.br";
    private static final String PRODUCTION_API_URL = "/api/secure/jsonws";

    @Provides @Singleton
    Portal providePortal() {
        return () -> PRODUCTION_PORTAL_URL;
    }

    @Provides @Singleton
    Endpoint provideEndpoint(Portal portal) {
        return () -> portal.url() + PRODUCTION_API_URL;
    }

    @Provides @Singleton
    EDMService provideEDMService(final Application application, Endpoint endpoint) {
        return new EDMServiceImpl.Builder()
                .setAuthentication(request -> {
                    Account account = AccountUtils.getAccount(application);
                    if (account != null) {
                        AccountManager manager = AccountManager.get(application);
                        String email = account.name;
                        String password = manager.getPassword(account);
                        new BasicAuthentication(email, password).authenticate(request);
                    }
                })
                .setEndpoint(endpoint)
                .build();
    }
}
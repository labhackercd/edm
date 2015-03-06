package net.labhackercd.edemocracia.data.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.data.api.client.Endpoint;

import org.apache.http.HttpRequest;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(complete = false, library = true)
@SuppressWarnings("UnusedDeclaration")
public class ApiModule {
    private static final String PRODUCTION_API_URL = "https://edemocracia.camara.gov.br/api/secure/jsonws";

    @Provides @Singleton
    Endpoint provideEndpoint() {
        return Endpoint.createFixed(PRODUCTION_API_URL);
    }

    @Provides @Singleton
    EDMService provideEDMService(final Application application, Endpoint endpoint) {
        return new EDMServiceImpl.Builder()
                .setAuthentication(new Authentication() {
                    @Override
                    public void authenticate(HttpRequest request) throws Exception {
                        Account account = AccountUtils.getAccount(application);
                        if (account != null) {
                            AccountManager manager = AccountManager.get(application);
                            String email = account.name;
                            String password = manager.getPassword(account);
                            new BasicAuthentication(email, password).authenticate(request);
                        }
                    }
                })
                .setEndpoint(endpoint)
                .build();
    }
}
package net.labhackercd.edemocracia.data.api;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.service.Session;

import net.labhackercd.edemocracia.account.CredentialStore;

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
    Authentication provideAuthentication(final CredentialStore credentialStore) {
        return new Authentication() {
            @Override
            public void authenticate(HttpRequest request) throws Exception {
                String credential = credentialStore.get();
                if (credential != null) {
                    request.addHeader("Authorization", credential);
                }
            }
        };
    }

    @Provides @Singleton
    Session provideSession(Endpoint endpoint, Authentication authentication) {
        return new EDMSession(endpoint, authentication);
    }
}

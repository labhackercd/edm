package net.labhackercd.edemocracia.data.api;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.edemocracia.account.Credentials;
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
    Authentication provideAuthentication(final Credentials.Provider credentialsProvider) {
        return new Authentication() {
            @Override
            public void authenticate(HttpRequest request) throws Exception {
                Credentials credentials = credentialsProvider.getCredentials();
                if (credentials != null) {
                    String email = credentials.getEmailAddress();
                    String password = credentials.getPassword();
                    new BasicAuthentication(email, password).authenticate(request);
                }
            }
        };
    }

    @Provides @Singleton
    EDMService provideEDMService(Endpoint endpoint, Authentication authentication) {
        return new EDMService.Builder()
                .setEndpoint(endpoint)
                .setErrorHandler(new EDMErrorHandler())
                .setAuthentication(authentication)
                .build();
    }
}

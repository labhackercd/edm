package net.labhackercd.edemocracia.data.api;

import com.liferay.mobile.android.auth.Authentication;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.liferay.mobile.android.service.Session;

import net.labhackercd.edemocracia.account.Credentials;

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
                    new BasicAuthentication(
                            credentials.getEmailAddress(), credentials.getPassword())
                                .authenticate(request);
                }
            }
        };
    }

    @Provides @Singleton
    Session provideSession(Endpoint endpoint, Authentication authentication) {
        return new EDMSession(endpoint, authentication);
    }
}

package net.labhackercd.edemocracia.data.api;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import net.labhackercd.edemocracia.data.api.error.EventBasedErrorHandler;
import net.romenor.deathray.LiferayAdapter;
import net.romenor.deathray.converter.GsonConverter;

import java.io.IOException;
import java.net.Proxy;
import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

@Module(complete=false, library=true)
@SuppressWarnings("UnusedDeclaration")
public class ApiModule {
    public static final String PRODUCTION_API_URL = "https://edemocracia.camara.gov.br/api/secure/jsonws";

    @Provides
    @Singleton
    SharedPreferencesCredentialStorage provideCredentials(Application app) {
        return new SharedPreferencesCredentialStorage(app);
    }

    @Provides
    @Singleton
    LiferayAdapter provideLiferayAdapter(EventBus eventBus, OkHttpClient client,
                                         final SharedPreferencesCredentialStorage credentials) {
        client.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String authorization = credentials.load();
                return response.request().newBuilder()
                        .addHeader("Authorization", authorization)
                        .build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();

        return new LiferayAdapter.Builder()
                .setEndpoint(PRODUCTION_API_URL)
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new EventBasedErrorHandler(eventBus))
                .setClient(client)
                .build();
    }

    @Provides
    @Singleton
    GroupService provideGroupService(LiferayAdapter adapter) {
        return adapter.create(GroupService.class);
    }
}

package net.labhackercd.edemocracia.data.api;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(complete = false, library = true)
@SuppressWarnings("UnusedDeclaration")
public class ApiModule {
    @Provides
    @Singleton
    SessionManager provideSessionManager(Application application) {
        return new SessionManager(application);
    }

    @Provides
    @Singleton
    EDMSession provideEDMSession(SessionManager sessionManager) {
        EDMSession session = sessionManager.load();

        if (session == null) {
            session = new EDMSession();
        }

        return session;
    }
}

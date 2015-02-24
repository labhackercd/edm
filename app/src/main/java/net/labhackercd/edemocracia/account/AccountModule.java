package net.labhackercd.edemocracia.account;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(complete = false, library = true)
@SuppressWarnings("UnusedDeclaration")
public class AccountModule {
    @Provides @Singleton
    CredentialStore provideCredentialStore(final SharedPreferences sharedPreferences) {
        return new SharedPreferencesCredentialStore(sharedPreferences);
    }
}
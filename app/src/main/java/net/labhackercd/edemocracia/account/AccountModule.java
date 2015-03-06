package net.labhackercd.edemocracia.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        complete = false,
        injects = {SignInActivity.class}
)
@SuppressWarnings("UnusedDeclaration")
public class AccountModule {
    @Provides @Singleton
    UserData provideUserData() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();
        return new UserData(gson);
    }
}
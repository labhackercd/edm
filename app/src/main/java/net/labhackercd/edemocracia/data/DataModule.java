package net.labhackercd.edemocracia.data;

import net.labhackercd.edemocracia.data.api.ApiModule;
import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.db.DbModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        complete = false,
        includes = {DbModule.class, ApiModule.class}
)
@SuppressWarnings("UnusedDeclaration")
public class DataModule {
    @Provides @Singleton
    Cache provideRequestCache() {
        return new LHMCache();
    }

    @Provides @Singleton
    MainRepository provideMainRepository(EDMService service) {
        return new MainRepository(service);
    }
}

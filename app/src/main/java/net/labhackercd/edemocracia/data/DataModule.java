package net.labhackercd.edemocracia.data;

import net.labhackercd.edemocracia.data.api.ApiModule;
import net.labhackercd.edemocracia.data.api.EDMService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = ApiModule.class,
        library = true,
        complete = false
)
@SuppressWarnings("UnusedDeclaration")
public class DataModule {
    @Provides @Singleton
    DataRepository provideDataRepository(EDMService service) {
        return new DataRepository(service);
    }
}

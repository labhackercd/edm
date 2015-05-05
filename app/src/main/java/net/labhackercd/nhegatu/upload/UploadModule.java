package net.labhackercd.nhegatu.upload;

import android.app.Application;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = false, library = true)
@SuppressWarnings("UnusedDeclaration")
public class UploadModule {
    @Provides @Singleton
    YouTubeUploader provideVideoUploader(Application application) {
        return new YouTubeUploader(application.getApplicationContext());
    }
}

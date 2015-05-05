package net.labhackercd.nhegatu.service;

import android.app.Application;
import dagger.Module;
import dagger.Provides;
import net.labhackercd.nhegatu.upload.YouTubeUploader;

import javax.inject.Singleton;

@Module(complete = false, library = true, injects = {
        AddMessageTask.class,
        AddMessageService.class
})
@SuppressWarnings("UnusedDeclaration")
public class ServiceModule {
    @Provides @Singleton
    VideoAttachmentUploader provideUploadManager(Application application, YouTubeUploader uploader) {
        return new VideoAttachmentUploader(application.getApplicationContext(), uploader);
    }
}

package net.labhackercd.edemocracia;

import android.app.Application;

import net.labhackercd.edemocracia.account.AccountModule;
import net.labhackercd.edemocracia.data.api.ApiModule;
import net.labhackercd.edemocracia.job.JobModule;
import net.labhackercd.edemocracia.job.VideoUploadJob;
import net.labhackercd.edemocracia.account.SignInActivity;

import javax.inject.Singleton;

import dagger.Provides;
import de.greenrobot.event.EventBus;

@dagger.Module(
        includes = {
                JobModule.class,
                ApiModule.class,
                AccountModule.class
        },
        injects = {
                SignInActivity.class,
                VideoUploadJob.class,
                SplashScreenActivity.class
        }
)
@SuppressWarnings("UnusedDeclaration")
class EDMRootModule {
    private final EDMApplication application;

    EDMRootModule(EDMApplication application) {
        this.application = application;
    }

    @Provides @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides @Singleton
    EventBus provideEventBus() {
        return EventBus.getDefault();
    }
}

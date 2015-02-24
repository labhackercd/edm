package net.labhackercd.edemocracia;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import net.labhackercd.edemocracia.account.AccountModule;
import net.labhackercd.edemocracia.data.api.ApiModule;
import net.labhackercd.edemocracia.job.JobModule;
import net.labhackercd.edemocracia.job.VideoUploadJob;
import net.labhackercd.edemocracia.ui.MainActivity;
import net.labhackercd.edemocracia.account.SignInActivity;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.ComposeActivity;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;

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

    @Provides @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return application.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }
}

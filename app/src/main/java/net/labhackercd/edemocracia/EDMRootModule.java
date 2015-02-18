package net.labhackercd.edemocracia;

import android.app.Application;

import net.labhackercd.edemocracia.data.api.ApiModule;
import net.labhackercd.edemocracia.data.api.EDMSession;
import net.labhackercd.edemocracia.data.api.SessionManager;
import net.labhackercd.edemocracia.job.JobModule;
import net.labhackercd.edemocracia.job.VideoUploadJob;
import net.labhackercd.edemocracia.ui.MainActivity;
import net.labhackercd.edemocracia.ui.SignInActivity;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.ui.SplashScreenActivity;
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
        },
        injects = {
                MainActivity.class,
                ComposeActivity.class,
                GroupListFragment.class,
                MessageListFragment.class,
                SignInActivity.class,
                SimpleRecyclerViewFragment.class,
                SplashScreenActivity.class,
                ThreadListFragment.class,
                VideoUploadJob.class
        }
)
@SuppressWarnings("UnusedDeclaration")
class EDMRootModule {
    private final EDMApplication application;

    EDMRootModule(EDMApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return EventBus.getDefault();
    }
}

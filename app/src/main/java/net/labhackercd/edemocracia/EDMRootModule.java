package net.labhackercd.edemocracia;

import android.content.Context;
import android.util.Log;

import com.path.android.jobqueue.BaseJob;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.di.DependencyInjector;
import com.path.android.jobqueue.log.CustomLogger;

import net.labhackercd.edemocracia.data.api.EDMSession;
import net.labhackercd.edemocracia.data.api.SessionManager;
import net.labhackercd.edemocracia.job.AddMessageJob;
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
        injects = {
                AddMessageJob.class,
                VideoUploadJob.class,
                ComposeActivity.class,
                MainActivity.class,
                SignInActivity.class,
                SplashScreenActivity.class,
                GroupListFragment.class,
                ThreadListFragment.class,
                MessageListFragment.class,
                SimpleRecyclerViewFragment.class
        }
)
class EDMRootModule {
    private SessionManager sessionManager;
    private final EDMApplication application;

    EDMRootModule(EDMApplication application) {
        this.application = application;
        this.sessionManager = null;
    }

    @Provides
    @SuppressWarnings("UnusedDeclaration")
    Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    @SuppressWarnings("UnusedDeclaration")
    EventBus provideBus() {
        return EventBus.getDefault();
    }

    @Provides
    @Singleton
    @SuppressWarnings("UnusedDeclaration")
    EDMSession provideEDMSession() {
        EDMSession session = provideSessionManager().load();

        if (session == null) {
            session = new EDMSession();
        }

        return session;
    }

    @Provides
    @Singleton
    @SuppressWarnings("UnusedDeclaration")
    SessionManager provideSessionManager() {
        if (sessionManager == null) {
            sessionManager = new SessionManager(application);
        }
        return sessionManager;
    }

    @Provides
    @Singleton
    @SuppressWarnings("UnusedDeclaration")
    JobManager provideJobManager() {
        Configuration configuration = new Configuration.Builder(application)
                .customLogger(new CustomLogger() {
                    public static final String TAG = "JobManager";

                    @Override
                    public boolean isDebugEnabled() {
                        return BuildConfig.DEBUG;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(0)
                .maxConsumerCount(3)
                .loadFactor(3)
                .consumerKeepAlive(3)
                .injector(new DependencyInjector() {
                    @Override
                    public void inject(BaseJob job) {
                        application.inject(this);
                    }
                })
                .build();
        return new JobManager(application, configuration);
    }
}

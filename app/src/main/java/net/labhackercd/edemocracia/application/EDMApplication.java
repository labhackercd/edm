package net.labhackercd.edemocracia.application;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.path.android.jobqueue.BaseJob;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.di.DependencyInjector;
import com.path.android.jobqueue.log.CustomLogger;

import dagger.ObjectGraph;
import dagger.Provides;

import de.greenrobot.event.EventBus;

import javax.inject.Singleton;

import net.labhackercd.edemocracia.BuildConfig;
import net.labhackercd.edemocracia.activity.MainActivity;
import net.labhackercd.edemocracia.activity.SignInActivity;
import net.labhackercd.edemocracia.activity.SplashScreenActivity;
import net.labhackercd.edemocracia.fragment.GroupListFragment;
import net.labhackercd.edemocracia.fragment.MessageListFragment;
import net.labhackercd.edemocracia.fragment.simplerecyclerview.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.fragment.ThreadListFragment;
import net.labhackercd.edemocracia.jobqueue.AddMessageJob;
import net.labhackercd.edemocracia.jobqueue.VideoUploadJob;
import net.labhackercd.edemocracia.liferay.session.SessionManager;
import net.labhackercd.edemocracia.liferay.session.EDMSession;

public class EDMApplication extends MultiDexApplication implements DependencyInjector {
    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new Module(this));
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    @Override
    public void inject(BaseJob job) {
        inject((Object) job);
    }

    @dagger.Module(
            injects = {
                    AddMessageJob.class,
                    VideoUploadJob.class,
                    MainActivity.class,
                    SignInActivity.class,
                    SplashScreenActivity.class,
                    GroupListFragment.class,
                    ThreadListFragment.class,
                    MessageListFragment.class,
                    SimpleRecyclerViewFragment.class
            }
    )
    static class Module {
        private SessionManager sessionManager;
        private final EDMApplication application;

        Module(EDMApplication application) {
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
                    .injector(application)
                    .build();
            return new JobManager(application, configuration);
        }
    }
}

package net.labhackercd.edemocracia.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.squareup.otto.Bus;

import dagger.ObjectGraph;
import dagger.Provides;

import javax.inject.Singleton;

import net.labhackercd.edemocracia.activity.MainActivity;
import net.labhackercd.edemocracia.activity.SignInActivity;
import net.labhackercd.edemocracia.activity.SplashScreenActivity;
import net.labhackercd.edemocracia.fragment.GroupListFragment;
import net.labhackercd.edemocracia.fragment.MessageListFragment;
import net.labhackercd.edemocracia.fragment.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.fragment.ThreadListFragment;
import net.labhackercd.edemocracia.liferay.session.SessionManager;
import net.labhackercd.edemocracia.task.AddMessageTaskQueue;
import net.labhackercd.edemocracia.task.AddMessageTaskService;
import net.labhackercd.edemocracia.task.VideoUploadTaskQueue;
import net.labhackercd.edemocracia.task.VideoUploadTaskService;
import net.labhackercd.edemocracia.liferay.session.EDMSession;

public class EDMApplication extends MultiDexApplication {
    private ObjectGraph objectGraph;

    private static final String SHARED_PREFERENCES = "net.labhackercd.EDMApplication";
    private static final String CREDENTIALS_KEY = "credentials";
    private static final String COMPANY_ID_KEY = "compnayId";

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new Module(this));
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    @dagger.Module(
            injects = {
                    AddMessageTaskService.class,
                    VideoUploadTaskService.class,
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
        private final Context applicationContext;

        Module(Context applicationContext) {
            this.sessionManager = null;
            this.applicationContext = applicationContext;
        }

        @Provides
        @Singleton
        @SuppressWarnings("UnusedDeclaration")
        AddMessageTaskQueue provideAddMessageTaskQueue(Gson gson, Bus bus) {
            return AddMessageTaskQueue.create(applicationContext, gson, bus);
        }

        @Provides
        @Singleton
        @SuppressWarnings("UnusedDeclaration")
        VideoUploadTaskQueue provideVideoUploadTaskQueue(Gson gson, Bus bus) {
            return VideoUploadTaskQueue.create(applicationContext, gson);
        }

        @Provides
        @Singleton
        @SuppressWarnings("UnusedDeclaration")
        Bus provideBus() {
            return new Bus();
        }

        @Provides
        @Singleton
        @SuppressWarnings("UnusedDeclaration")
        Gson provideGson() {
            return new GsonBuilder().create();
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
                sessionManager = new SessionManager(applicationContext);
            }
            return sessionManager;
        }
    }
}

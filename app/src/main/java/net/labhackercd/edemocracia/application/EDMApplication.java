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
import net.labhackercd.edemocracia.fragment.ThreadListFragment;
import net.labhackercd.edemocracia.task.AddMessageTaskQueue;
import net.labhackercd.edemocracia.task.AddMessageTaskService;
import net.labhackercd.edemocracia.task.VideoUploadTaskQueue;
import net.labhackercd.edemocracia.task.VideoUploadTaskService;
import net.labhackercd.edemocracia.util.EDMSession;

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

    public void saveEDMSession(EDMSession session) {
        SharedPreferences.Editor editor = getApplicationContext()
                .getSharedPreferences(SHARED_PREFERENCES, 0).edit();

        editor.putString(CREDENTIALS_KEY, new Gson().toJson(session.getAuthentication()));
        editor.putLong(COMPANY_ID_KEY, session.getCompanyId());

        editor.apply();
    }

    private EDMSession loadEDMSession() {
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(SHARED_PREFERENCES, 0);

        String json = sharedPreferences.getString(CREDENTIALS_KEY, "null");

        long companyId = sharedPreferences.getLong(COMPANY_ID_KEY, -1);
        BasicAuthentication credentials = new Gson().fromJson(json, BasicAuthentication.class);

        return new EDMSession(credentials, companyId);
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
                    MessageListFragment.class
            }
    )
    static class Module {
        private final Context applicationContext;

        Module(Context applicationContext) {
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
            EDMSession session = ((EDMApplication) applicationContext).loadEDMSession();

            if (session == null) {
                session = new EDMSession();
            }

            return session;
        }
    }
}

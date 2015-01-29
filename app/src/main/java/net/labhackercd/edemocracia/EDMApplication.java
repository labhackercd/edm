package net.labhackercd.edemocracia;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import net.labhackercd.edemocracia.tasks.VideoUploadTaskQueue;
import net.labhackercd.edemocracia.tasks.VideoUploadTaskService;
import dagger.ObjectGraph;
import dagger.Provides;

import net.labhackercd.edemocracia.tasks.AddMessageTaskService;
import net.labhackercd.edemocracia.tasks.AddMessageTaskQueue;

public class EDMApplication extends Application {
    private ObjectGraph objectGraph;

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
                    MainActivity.class,
                    AddMessageTaskService.class,
                    VideoUploadTaskService.class
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
    }
}

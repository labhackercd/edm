package br.leg.camara.labhacker.edemocracia;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import br.leg.camara.labhacker.edemocracia.tasks.AddMessageTaskService;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import br.leg.camara.labhacker.edemocracia.tasks.AddMessageTaskQueue;

public class EDMApplication extends Application {
    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new DaggerModule(this));
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    @Module(
            injects = {
                    MainActivity.class,
                    AddMessageTaskService.class
            }
    )
    static class DaggerModule {
        private final Context applicationContext;

        DaggerModule(Context applicationContext) {
            this.applicationContext = applicationContext;
        }

        @SuppressWarnings("UnusedDeclaration")
        @Provides
        @Singleton
        AddMessageTaskQueue provideAddMessageTaskQueue(Gson gson, Bus bus) {
            return AddMessageTaskQueue.create(applicationContext, gson, bus);
        }


        @SuppressWarnings("UnusedDeclaration")
        @Provides
        @Singleton
        Bus provideBus() {
            return new Bus();
        }

        @SuppressWarnings("UnusedDeclaration")
        @Provides
        @Singleton
        Gson provideGson() {
            return new GsonBuilder().create();
        }
    }
}

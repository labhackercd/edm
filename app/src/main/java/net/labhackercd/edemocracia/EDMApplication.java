package net.labhackercd.edemocracia;

import android.app.Application;

import dagger.ObjectGraph;

public class EDMApplication extends Application {
    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new EDMRootModule(this));
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }
}

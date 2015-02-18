package net.labhackercd.edemocracia;

import android.support.multidex.MultiDexApplication;

import com.path.android.jobqueue.BaseJob;
import com.path.android.jobqueue.di.DependencyInjector;

import dagger.ObjectGraph;

public class EDMApplication extends MultiDexApplication {
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

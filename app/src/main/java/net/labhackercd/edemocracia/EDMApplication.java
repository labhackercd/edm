package net.labhackercd.edemocracia;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;
import timber.log.Timber;

public class EDMApplication extends Application {
    private ObjectGraph objectGraph;

    public static EDMApplication get(Context context) {
        return (EDMApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        objectGraph = ObjectGraph.create(new EDMRootModule(this));

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    public void inject(Object object) {
        objectGraph.inject(object);
    }

    /**
     * A Timber Tree that logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.HollowTree {
        // TODO Override methods, integrate into some crash reporting service.
    }
}

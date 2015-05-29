package net.labhackercd.nhegatu;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;
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
            Fabric.with(this, new Crashlytics());
            // TODO {@link Timber.plant} something?
        }
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }
}

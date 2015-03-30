package net.labhackercd.edemocracia;

import android.app.Application;
import android.content.Context;
import android.util.Log;

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
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void logMessage(int priority, String tag, String message) {
                    // XXX We don't call super because we don't want it splitting long messages.
                    Log.println(priority, tag, message);
                }
            });
        } else {
            Fabric.with(this, new Crashlytics());
            Timber.plant(new CrashReportingTree());
        }
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    /**
     * A Timber Tree that logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.HollowTree {
        @Override
        public void e(String message, Object... args) {
            Fabric.getLogger().e("Timber", String.format(message, args));
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            Fabric.getLogger().e("Timber", String.format(message, args), t);
        }

        @Override
        public void w(String message, Object... args) {
            Fabric.getLogger().w("Timber", String.format(message, args));
        }

        @Override
        public void w(Throwable t, String message, Object... args) {
            Fabric.getLogger().w("Timber", String.format(message, args), t);
        }
    }
}

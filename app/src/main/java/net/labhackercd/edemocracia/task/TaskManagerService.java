package net.labhackercd.edemocracia.task;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import net.labhackercd.edemocracia.EDMApplication;

import javax.inject.Inject;

import rx.functions.Action0;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A service that is used by the TaskManager to execute tasks.
 *
 * Currently it only execute one task at a time, but that can change in the future.
 */
public class TaskManagerService extends Service {
    @Inject TaskManager taskManager;

    private boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
        EDMApplication.get(getApplicationContext()).getObjectGraph().inject(this);
        Timber.d("Service created.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return Service.START_STICKY;
    }

    private void executeNext() {
        if (running) return;
        Task task = taskManager.peek();
        if (task != null) {
            running = true;
            // Each task should run in a new thread.
            Schedulers.newThread().createWorker().schedule(new Action0() {
                @Override
                public void call() {
                    task.execute(new Task.Callback() {
                        @Override
                        public void onCompleted() {
                            taskManager.onTaskCompleted(task);
                            running = false;
                            executeNext();
                        }

                        @Override
                        public void onError(Throwable error) {
                            taskManager.onTaskError(task, error);
                            running = false;
                            executeNext();
                        }
                    });
                }
            });
        } else {
            Timber.d("Stopping service...");
            stopSelf();
        }
    }
}

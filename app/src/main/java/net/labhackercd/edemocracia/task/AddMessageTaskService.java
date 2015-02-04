package net.labhackercd.edemocracia.task;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import javax.inject.Inject;

import net.labhackercd.edemocracia.application.EDMApplication;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.liferay.session.EDMSession;

import de.greenrobot.event.EventBus;

public class AddMessageTaskService extends Service implements AddMessageTask.Callback {
    private static final String TAG = AddMessageTaskService.class.getSimpleName();

    @Inject EventBus eventBus;
    @Inject AddMessageTaskQueue queue;
    @Inject EDMSession session;

    private boolean running;

    @Override
    public void onCreate() {
        super.onCreate();
        ((EDMApplication) getApplication()).inject(this);
        Log.i(TAG, "Starting service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return START_STICKY;
    }

    @Override
    public void onSuccess(Message message) {
        running = false;
        queue.remove();
        eventBus.post(new AddMessageTask.Success(message));
        executeNext();
    }

    @Override
    public void onFailure(Message message, Exception e) {
        Log.e(TAG, "Failed to submit message: " + e.toString());

        running = false;
        eventBus.post(new AddMessageTask.Failure(message, e));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void executeNext() {
        if (running) {
            return;
        }

        AddMessageTask task = queue.peek();

        if (task != null) {
            running = true;
            task.execute(session, this);
        } else {
            Log.i(TAG, "Stopping service");
            stopSelf();
        }
    }
}

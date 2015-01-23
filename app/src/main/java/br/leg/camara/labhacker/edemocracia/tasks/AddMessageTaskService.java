package br.leg.camara.labhacker.edemocracia.tasks;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.tape.ObjectQueue;

import javax.inject.Inject;

import br.leg.camara.labhacker.edemocracia.EDMApplication;
import br.leg.camara.labhacker.edemocracia.content.Message;

public class AddMessageTaskService extends Service implements AddMessageTask.Callback {
    private static final String TAG = AddMessageTaskService.class.getSimpleName();

    @Inject Bus bus;
    @Inject AddMessageTaskQueue queue;

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
        bus.post(new AddMessageSuccessEvent(message));
        executeNext();
    }

    @Override
    public void onFailure(Message message, Exception e) {
        Log.e(TAG, "Failed to submit message: " + e.toString());

        running = false;
        bus.post(new AddMessageFailureEvent(message, e));
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
            task.execute(getApplication(), this);
        } else {
            Log.i(TAG, "Stopping service");
            stopSelf();
        }
    }
}

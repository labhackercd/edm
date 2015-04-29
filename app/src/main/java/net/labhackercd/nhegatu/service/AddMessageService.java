package net.labhackercd.nhegatu.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.nhegatu.EDMApplication;
import net.labhackercd.nhegatu.data.db.LocalMessage;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A service that processes LocalMessages.
 */
public class AddMessageService extends Service {
    @Inject SqlBrite brite;

    private boolean running = false;
    private Subscription subscription;

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
        // FIXME We can only process one item at time.
        if (running) return;

        // TODO Prioritize current user's messages? :D

        Observable<SqlBrite.Query> query = brite.createQuery(
                LocalMessage.TABLE,
                QUERY_FIRST_MESSAGE_BY_STATUS,
                LocalMessage.valueOf(LocalMessage.Status.QUEUE));

        LocalMessage message = query.map(SqlBrite.Query::run)
                .map(LocalMessage.READ_SINGLE)
                .filter(m -> m != null)
                .toBlocking()
                .firstOrDefault(null);

        if (message == null) {
            Timber.d("Stopping service...");
            stopSelf();
            return;
        }

        AddMessageTask task = new AddMessageTask(message);

        EDMApplication.get(this).getObjectGraph().inject(task);

        subscription = Schedulers.newThread().createWorker().schedule(() -> {
            try {
                task.execute();
            } catch (Throwable t) {
                task.onError(t);
            }

            running = false;
            executeNext();
        });
    }

    private static final String QUERY_FIRST_MESSAGE_BY_STATUS =
            "SELECT * FROM " + LocalMessage.TABLE + " WHERE " + LocalMessage.STATUS + " = ? LIMIT 1";
}

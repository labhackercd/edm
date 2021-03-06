/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

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

        // We're running, baby!
        running = true;

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

        // TODO FIXME When the task fails, if the user keeps retrying, it'll be executing this line N+1 times for every retry.

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

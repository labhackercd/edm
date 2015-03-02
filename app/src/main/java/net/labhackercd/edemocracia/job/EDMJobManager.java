package net.labhackercd.edemocracia.job;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.path.android.jobqueue.JobManager;

import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.DatabaseProvider;
import net.labhackercd.edemocracia.data.db.model.LocalMessage;

import java.util.List;
import java.util.UUID;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.DatabaseCompartment;
import rx.Observable;
import rx.schedulers.Schedulers;

public class EDMJobManager {
    private final JobManager jobManager;
    private final DatabaseProvider databaseHelper;

    public EDMJobManager(JobManager jobManager, DatabaseProvider databaseHelper) {
        this.jobManager = jobManager;
        this.databaseHelper = databaseHelper;
    }

    public LocalMessage addMessage(Message parentMessage, String subject,
                                   String body, Uri videoAttachment, String youtubeAccount) {
        LocalMessage message = new LocalMessage();
        message.uuid = UUID.randomUUID();
        message.rootMessageId = parentMessage.getRootMessageId();
        message.parentMessage = parentMessage;
        message.videoAttachment = videoAttachment;
        message.subject = subject;
        message.body = body;

        addMessageJob(message, youtubeAccount);

        return message;
    }

    private long addMessageJob(LocalMessage message, String youtubeAccount) {
        // Everything should happen inside a transaction to guarantee that both the
        // LocalMessage and the Job will be correctly added.
        Cupboard cupboard = databaseHelper.getCupboard();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.beginTransaction();

        DatabaseCompartment dbc = cupboard.withDatabase(db);

        Throwable error = null;
        try {
            long messageId = dbc.put(message);
            return jobManager.addJob(new AddMessageJob(messageId, youtubeAccount));
        } catch (Throwable throwable) {
            error = throwable;
            throw new RuntimeException(error);
        } finally {
            if (error == null)
                db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public Observable<List<LocalMessage>> getUnsentMessages(long rootMessageId) {
        final DatabaseCompartment dbc = databaseHelper.getCupboard()
                .withDatabase(databaseHelper.getWritableDatabase());
        return Observable.defer(() -> {
            return Observable.just(
                    dbc.query(LocalMessage.class)
                            .withProjection()
                            .withSelection(
                                    "rootMessageId = ? AND insertedMessageId IS NULL",
                                    Long.toString(rootMessageId))
                            .list());
        }).subscribeOn(Schedulers.io());
    }
}

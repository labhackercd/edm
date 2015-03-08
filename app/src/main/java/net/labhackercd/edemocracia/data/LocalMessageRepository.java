package net.labhackercd.edemocracia.data;

import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;

import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.DatabaseProvider;
import net.labhackercd.edemocracia.data.db.model.LocalMessage;
import net.labhackercd.edemocracia.task.AddMessageTask;
import net.labhackercd.edemocracia.task.TaskManager;

import java.util.List;
import java.util.UUID;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.DatabaseCompartment;
import rx.Observable;

public class LocalMessageRepository {
    private final TaskManager taskManager;
    private final DatabaseProvider databaseHelper;

    public LocalMessageRepository(TaskManager taskManager, DatabaseProvider databaseHelper) {
        this.taskManager = taskManager;
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

        createTask(message, youtubeAccount);

        return message;
    }

    private void createTask(LocalMessage message, String youtubeAccount) {
        // Everything should happen inside a transaction to guarantee that both the
        // LocalMessage and the Task are added.
        Cupboard cupboard = databaseHelper.getCupboard();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.beginTransaction();

        DatabaseCompartment dbc = cupboard.withDatabase(db);

        Throwable error = null;
        try {
            long messageId = dbc.put(message);
            taskManager.add(new AddMessageTask(messageId, youtubeAccount));
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
        final DatabaseCompartment dbc = databaseHelper
                .getCupboard()
                .withDatabase(databaseHelper.getWritableDatabase());

        return Observable.create(subscriber -> {
            List<LocalMessage> messages;
            try {
                messages = dbc.query(LocalMessage.class)
                        .withProjection()
                        .withSelection(
                                "rootMessageId = ? AND insertedMessageId IS NULL",
                                Long.toString(rootMessageId))
                        .list();
            } catch (Throwable t) {
                subscriber.onError(t);
                return;
            }
            subscriber.onNext(messages);
            subscriber.onCompleted();
        });
    }
}

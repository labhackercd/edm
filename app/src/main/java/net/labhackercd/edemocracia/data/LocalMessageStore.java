package net.labhackercd.edemocracia.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.service.AddMessageService;

import java.util.List;
import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class LocalMessageStore {
    private final SqlBrite brite;
    private final Context context;
    private final Subscription subscription;

    public LocalMessageStore(Context context, SqlBrite brite) {
        this.brite = brite;
        this.context = context;

        // XXX Oh yes, oh yeah, OH YES, OH YEAH!
        this.subscription = brite.createQuery(LocalMessage.TABLE,
                "SELECT COUNT(" + LocalMessage.ID + ") " +
                        "FROM " + LocalMessage.TABLE + " " +
                        "WHERE " + LocalMessage.STATUS + " = ?",
                LocalMessage.getStatusValue(LocalMessage.Status.QUEUE))
                .map(SqlBrite.Query::run)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(cursor -> {
                    cursor.moveToFirst();
                    long count = cursor.getLong(0);
                    if (count > 0)
                        startService();
                });
    }

    private void startService() {
        context.startService(new Intent(context, AddMessageService.class));
    }

    // TODO Should return LocalMessage but there isn't a LocalMessageBuilder yet.
    public long insert(Message parentMessage, String subject, String body, Uri videoAttachment) {
        LocalMessage.Builder builder = new LocalMessage.Builder()
                .rootMessageId(parentMessage.getRootMessageId())
                .groupId(parentMessage.getGroupId())
                .categoryId(parentMessage.getCategoryId())
                .threadId(parentMessage.getThreadId())
                .parentMessageId(parentMessage.getMessageId())
                .subject(subject)
                .body(body)
                .videoAttachment(videoAttachment)
                .uuid(UUID.randomUUID())
                .status(LocalMessage.Status.QUEUE);
        return brite.insert(LocalMessage.TABLE, builder.build());
    }

    public Observable<List<LocalMessage>> getUnsentMessages(long rootMessageId) {
        return LocalMessage.getUnsentMessages(brite, rootMessageId);
    }

    public Observable<LocalMessage> getMessage(long messageId) {
        return LocalMessage.getMessage(brite, messageId);
    }

    public int setSuccess(long id, Message inserted) {
        LocalMessage.Builder builder = new LocalMessage.Builder()
                .insertedMessageId(inserted.getMessageId())
                .insertionDate(inserted.getCreateDate())
                .status(LocalMessage.Status.SUCCESS);
        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.ID + " = ?", String.valueOf(id));
    }

    public int setStatus(long id, LocalMessage.Status status) {
        LocalMessage.Builder builder = new LocalMessage.Builder().status(status);
        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.ID + " = ?", String.valueOf(id));
    }

    public int retry(LocalMessage message) {
        return setStatus(message.id(), LocalMessage.Status.QUEUE);
    }

    public int retryAll(LocalMessage.Status status) {
        if (LocalMessage.Status.SUCCESS.equals(status))
            throw new IllegalArgumentException("status == SUCCESS");

        LocalMessage.Builder builder = new LocalMessage.Builder().status(LocalMessage.Status.QUEUE);

        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.STATUS + " = ?", LocalMessage.getStatusValue(status));
    }
}

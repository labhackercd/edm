package net.labhackercd.edemocracia.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.support.v4.util.Pair;
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
                LocalMessage.valueOf(LocalMessage.Status.QUEUE))
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
    public Pair<Long, UUID> insert(Message parentMessage, String subject, String body, Uri videoAttachment) {
        UUID uuid = UUID.randomUUID();
        LocalMessage.Builder builder = new LocalMessage.Builder()
                .setRootMessageId(parentMessage.getRootMessageId())
                .setGroupId(parentMessage.getGroupId())
                .setCategoryId(parentMessage.getCategoryId())
                .setThreadId(parentMessage.getThreadId())
                .setParentMessageId(parentMessage.getMessageId())
                .setSubject(subject)
                .setBody(body)
                .setVideoAttachment(videoAttachment)
                .setUuid(uuid)
                .setStatus(LocalMessage.Status.QUEUE);
        long id = brite.insert(LocalMessage.TABLE, builder.build());
        return new Pair<>(id, uuid);
    }

    public Observable<List<LocalMessage>> getUnsentMessages(long rootMessageId) {
        return LocalMessage.getUnsentMessages(brite, rootMessageId);
    }

    public Observable<SqlBrite.Query> getUnsentMessages2(long rootMessageId) {
        return LocalMessage.getUnsentMessages2(brite, rootMessageId);
    }

    public Observable<LocalMessage> getMessage(long messageId) {
        return LocalMessage.getMessage(brite, messageId);
    }

    public int setSuccess(long id, Message inserted) {
        LocalMessage.Builder builder = new LocalMessage.Builder()
                .setMessageId(inserted.getMessageId())
                .setCreateDate(inserted.getCreateDate())
                .setStatus(LocalMessage.Status.SUCCESS);
        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.ID + " = ?", String.valueOf(id));
    }

    public int setStatus(long id, LocalMessage.Status status) {
        LocalMessage.Builder builder = new LocalMessage.Builder().setStatus(status);
        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.ID + " = ?", String.valueOf(id));
    }

    public int retry(UUID uuid) {
        LocalMessage.Builder builder = new LocalMessage.Builder().setStatus(LocalMessage.Status.QUEUE);
        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.UUID + " = ? AND " + LocalMessage.STATUS + " != ?",
                LocalMessage.valueOf(uuid),
                LocalMessage.valueOf(LocalMessage.Status.SUCCESS));
    }

    public int retryAll(LocalMessage.Status status) {
        if (LocalMessage.Status.SUCCESS.equals(status))
            throw new IllegalArgumentException("status == SUCCESS");

        LocalMessage.Builder builder = new LocalMessage.Builder().setStatus(LocalMessage.Status.QUEUE);

        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.STATUS + " = ?", LocalMessage.valueOf(status));
    }
}

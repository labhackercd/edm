package net.labhackercd.edemocracia.data;

import android.net.Uri;

import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.task.AddMessageTask;
import net.labhackercd.edemocracia.task.TaskManager;

import java.util.List;
import java.util.UUID;

import rx.Observable;

public class LocalMessageRepository {
    private final SqlBrite brite;
    private final TaskManager taskManager;

    public LocalMessageRepository(TaskManager taskManager, SqlBrite brite) {
        this.brite = brite;
        this.taskManager = taskManager;
    }

    public long add(Message parentMessage, String subject, String body, Uri videoAttachment) {
        brite.beginTransaction();
        Throwable error = null;
        try {
            long id = brite.insert(LocalMessage.TABLE, new LocalMessage.Builder()
                    .rootMessageId(parentMessage.getRootMessageId())
                    .groupId(parentMessage.getGroupId())
                    .categoryId(parentMessage.getCategoryId())
                    .threadId(parentMessage.getThreadId())
                    .parentMessageId(parentMessage.getMessageId())
                    .subject(subject)
                    .body(body)
                    .videoAttachment(videoAttachment)
                    .uuid(UUID.randomUUID())
                    .status(LocalMessage.Status.QUEUE)
                    .build());
            taskManager.add(new AddMessageTask(id));
            return id;
        } catch (Throwable throwable) {
            error = throwable;
            throw new RuntimeException(error);
        } finally {
            if (error == null)
                brite.setTransactionSuccessful();
            brite.endTransaction();
        }
    }

    public void retry(LocalMessage localMessage) {
        if (localMessage.id() == null)
            throw new AssertionError("Can't retry a detached message instance.");

        if (!LocalMessage.Status.CANCEL.equals(localMessage.status()))
            throw new AssertionError("Can't retry a non-cancelled message instance.");

        brite.beginTransaction();
        Throwable error = null;
        try {
            brite.update(LocalMessage.TABLE, new LocalMessage.Builder()
                            .status(LocalMessage.Status.RETRY)
                            .build(),
                    LocalMessage.ID + " = ?", String.valueOf(localMessage.id()));
            taskManager.add(new AddMessageTask(localMessage.id()));
        } catch (Throwable throwable) {
            error = throwable;
            throw new RuntimeException(error);
        } finally {
            if (error == null)
                brite.setTransactionSuccessful();
            brite.endTransaction();
        }
    }

    public Observable<List<LocalMessage>> getUnsentMessages(long rootMessageId) {
        return LocalMessage.getUnsentMessages(brite, rootMessageId);
    }

    public Observable<LocalMessage> getMessage(long messageId) {
        return LocalMessage.getMessage(brite, messageId);
    }

    public void setSuccess(long id, Message inserted) {
        brite.update(LocalMessage.TABLE, new LocalMessage.Builder()
                        .insertedMessageId(inserted.getMessageId())
                        .insertionDate(inserted.getCreateDate())
                        .status(LocalMessage.Status.SUCCESS)
                        .build(),
                LocalMessage.ID + " = ?", String.valueOf(id));
    }

    public void setCancel(long id) {
        brite.update(LocalMessage.TABLE, new LocalMessage.Builder()
                        .status(LocalMessage.Status.CANCEL)
                        .build(),
                LocalMessage.ID + " = ?", String.valueOf(id));
    }
}

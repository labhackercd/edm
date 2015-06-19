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

package net.labhackercd.nhegatu.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.support.v4.util.Pair;
import com.squareup.sqlbrite.SqlBrite;

import net.labhackercd.nhegatu.data.model.Message;
import net.labhackercd.nhegatu.data.db.LocalMessage;
import net.labhackercd.nhegatu.service.AddMessageService;

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
        subscription = brite.createQuery(LocalMessage.TABLE,
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

    public Observable<List<LocalMessage>> getUnsentMessages(long userId, long rootMessageId) {
        return LocalMessage.createQueryForUnsentMessages(brite, userId, rootMessageId)
                .map(SqlBrite.Query::run)
                .map(LocalMessage.READ_LIST);
    }

    // TODO Should return LocalMessage but there isn't a LocalMessageBuilder yet.
    public Pair<Long, UUID> insert(Message parentMessage, long userId, String subject, String body, Uri videoAttachment) {
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
                .setStatus(LocalMessage.Status.QUEUE)
                .setUserId(userId);
        long id = brite.insert(LocalMessage.TABLE, builder.build());
        return new Pair<>(id, uuid);
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

    public int replaceAttachmentWithYouTubeVideo(LocalMessage message, String videoId) {
        String newBody = attachVideo(message.getBody(), videoId);
        LocalMessage.Builder builder = new LocalMessage.Builder()
                .setVideoAttachment(null)
                .setBody(newBody);
        return brite.update(LocalMessage.TABLE, builder.build(),
                LocalMessage.ID + " = ?", String.valueOf(message.getId()));
    }

    /** Attach a YouTube video to a message body and returns it (the body with the attached video). */
    public static String attachVideo(String body, String videoId) {
        return String.format("[center][youtube]%s[/youtube][/center]", videoId).concat(body);
    }
}

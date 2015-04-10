package net.labhackercd.edemocracia.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import auto.parcel.AutoParcel;
import net.labhackercd.edemocracia.data.model.Message;
import rx.Observable;
import rx.functions.Func1;

@AutoParcel
public abstract class LocalMessage implements Parcelable, Message {

    public enum Status {
        QUEUE,
        SUCCESS,
        CANCEL,
        CANCEL_RECOVERABLE_AUTH_ERROR
    }


    /** XXX Please, please, don't change the order of the abstract getters before reading AutoValues documentation. */

    public abstract long getId();
    public abstract long getMessageId();
    public abstract long getRootMessageId();
    public abstract long getGroupId();
    public abstract long getCategoryId();
    public abstract long getThreadId();
    public abstract long getParentMessageId();
    @Nullable public abstract String getBody();
    @Nullable public abstract String getSubject();
    @Nullable public abstract Uri getVideoAttachment();
    public abstract UUID getUuid();
    public abstract Status getStatus();
    @Nullable public abstract Date getCreateDate();
    public abstract long getUserId();

    public static final String TABLE = "LocalMessage";

    public static final String ID = "_id";
    public static final String MESSAGE_ID = "insertedMessageId";
    public static final String ROOT_MESSAGE_ID = "rootMessageId";
    public static final String GROUP_ID = "groupId";
    public static final String CATEGORY_ID = "categoryId";
    public static final String THREAD_ID = "threadId";
    public static final String PARENT_MESSAGE_ID = "parentMessageId";
    public static final String BODY = "body";
    public static final String SUBJECT = "subject";
    public static final String VIDEO_ATTACHMENT = "videoAttachment";
    public static final String UUID = "uuid";
    public static final String STATUS = "status";
    public static final String CREATE_DATE = "insertionDate";
    public static final String USER_ID = "userId";

    static final String CREATE = Joiner.on('\n').join(
            "CREATE TABLE " + TABLE + "(",
                ID + " INTEGER NOT NULL PRIMARY KEY,",
                MESSAGE_ID + " INTEGER NOT NULL DEFAULT 0,",
                ROOT_MESSAGE_ID + " INTEGER NOT NULL,",
                PARENT_MESSAGE_ID + " INTEGER NOT NULL,",
                GROUP_ID + " INTEGER NOT NULL,",
                CATEGORY_ID + " INTEGER NOT NULL,",
                THREAD_ID + " INTEGER NOT NULL,",
                BODY + " TEXT,",
                SUBJECT + " TEXT,",
                VIDEO_ATTACHMENT + " TEXT,",
                UUID + " TEXT NOT NULL,",
                STATUS + " TEXT NOT NULL,",
                CREATE_DATE + " INTEGER,",
                USER_ID + " INTEGER NOT NULL"
            + ")");


    static final String INDEX_ROOT_MESSAGE_ID = "idxRootMessageId";
    static final String INDEX_INSERTED_MESSAGE_ID = "idxInsertedMessageId";

    static final String CREATE_INDEX_ROOT_MESSAGE_ID =
            "CREATE INDEX " + INDEX_ROOT_MESSAGE_ID + " ON " + TABLE + " (" + ROOT_MESSAGE_ID + ")";

    static final String CREATE_INDEX_INSERTED_MESSAGE_ID =
            "CREATE INDEX " + INDEX_INSERTED_MESSAGE_ID + " ON " + TABLE + " (" + MESSAGE_ID + ")";


    public static Observable<SqlBrite.Query> getUnsentMessages2(SqlBrite brite, long rootMessageId) {
        return brite.createQuery(TABLE, "SELECT * FROM " + TABLE + " WHERE " + ROOT_MESSAGE_ID + " = ?",
                String.valueOf(rootMessageId));
    }

    public static Observable<List<LocalMessage>> getUnsentMessages(
            SqlBrite brite, long rootMessageId) {
        return getUnsentMessages2(brite, rootMessageId)
                .map(SqlBrite.Query::run)
                .map(READ_LIST);
    }

    public static Observable<LocalMessage> getMessage(SqlBrite brite, long messageId) {
        return brite.createQuery(TABLE, "SELECT * FROM " + TABLE + " WHERE " + ID + " = ?",
                String.valueOf(messageId))
                .map(SqlBrite.Query::run)
                .map(READ_SINGLE);
    }

    public static final Func1<Cursor, List<LocalMessage>> READ_LIST = cursor -> {
        try {
            List<LocalMessage> list = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext())
                list.add(fromCursor(cursor));
            return list;
        } finally {
            cursor.close();
        }
    };

    public static final Func1<Cursor, LocalMessage> READ_SINGLE = cursor -> {
        List<LocalMessage> result = READ_LIST.call(cursor);
        return result.size() > 0 ? result.get(0) : null;
    };

    private static LocalMessage fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
        long messageId = cursor.getLong(cursor.getColumnIndexOrThrow(MESSAGE_ID));
        long rootMessageId = cursor.getLong(cursor.getColumnIndexOrThrow(ROOT_MESSAGE_ID));
        long groupId = cursor.getLong(cursor.getColumnIndexOrThrow(GROUP_ID));
        long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(CATEGORY_ID));
        long threadId = cursor.getLong(cursor.getColumnIndexOrThrow(THREAD_ID));
        long parentMessageId = cursor.getLong(cursor.getColumnIndexOrThrow(PARENT_MESSAGE_ID));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(BODY));
        String subject = cursor.getString(cursor.getColumnIndexOrThrow(SUBJECT));
        Uri videoAttachment = nullableUri(
                cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_ATTACHMENT)));
        UUID uuid = java.util.UUID.fromString(
                cursor.getString(cursor.getColumnIndexOrThrow(UUID)));
        Status status = Enum.valueOf(
                Status.class, cursor.getString(cursor.getColumnIndexOrThrow(STATUS)));
        Date insertionDate = nullableDate(
                cursor.getLong(cursor.getColumnIndexOrThrow(CREATE_DATE)));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(USER_ID));
        return new AutoParcel_LocalMessage(
                id, messageId, rootMessageId, groupId, categoryId, threadId,
                parentMessageId, body, subject, videoAttachment, uuid, status, insertionDate, userId);
    }

    private static Date nullableDate(Long milliseconds) {
        return milliseconds == null ? null : new Date(milliseconds);
    }

    private static Uri nullableUri(String string) {
        return string == null ? null : Uri.parse(string);
    }

    public static String valueOf(Status status) {
        return status.name();
    }

    public static String valueOf(UUID uuid) {
        return uuid.toString();
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(ID, id);
            return this;
        }

        public Builder setMessageId(long messageId) {
            values.put(MESSAGE_ID, messageId);
            return this;
        }

        public Builder setRootMessageId(long rootMessageId) {
            values.put(ROOT_MESSAGE_ID, rootMessageId);
            return this;
        }

        public Builder setGroupId(long groupId) {
            values.put(GROUP_ID, groupId);
            return this;
        }

        public Builder setCategoryId(long categoryId) {
            values.put(CATEGORY_ID, categoryId);
            return this;
        }

        public Builder setThreadId(long threadId) {
            values.put(THREAD_ID, threadId);
            return this;
        }

        public Builder setParentMessageId(long parentMessageId) {
            values.put(PARENT_MESSAGE_ID, parentMessageId);
            return this;
        }

        public Builder setBody(String body) {
            if (body != null)
                values.put(BODY, body);
            else
                values.putNull(BODY);
            return this;
        }

        public Builder setSubject(String subject) {
            if (subject != null)
                values.put(SUBJECT, subject);
            else
                values.putNull(SUBJECT);
            return this;
        }

        public Builder setVideoAttachment(Uri videoAttachment) {
            if (videoAttachment != null)
                values.put(VIDEO_ATTACHMENT, videoAttachment.toString());
            else
                values.putNull(VIDEO_ATTACHMENT);
            return this;
        }

        public Builder setUuid(UUID uuid) {
            values.put(UUID, valueOf(uuid));
            return this;
        }

        public Builder setStatus(Status status) {
            values.put(STATUS, valueOf(status));
            return this;
        }

        public Builder setCreateDate(Date date) {
            if (date != null)
                values.put(CREATE_DATE, date.getTime());
            else
                values.putNull(CREATE_DATE);
            return this;
        }

        public Builder setUserId(long userId) {
            values.put(USER_ID, userId);
            return this;
        }

        public ContentValues build() {
            return new ContentValues(values);
        }
    }
}

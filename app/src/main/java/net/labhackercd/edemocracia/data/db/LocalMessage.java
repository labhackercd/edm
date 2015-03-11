package net.labhackercd.edemocracia.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import auto.parcel.AutoParcel;
import rx.Observable;
import rx.functions.Func1;

@AutoParcel
public abstract class LocalMessage implements Parcelable {

    public enum Status {
        QUEUE,
        SUCCESS,
        CANCEL,
        RETRY
    }

    public abstract Long id();
    @Nullable public abstract Long insertedMessageId();
    public abstract Long rootMessageId();
    public abstract Long groupId();
    public abstract Long categoryId();
    public abstract Long threadId();
    public abstract Long parentMessageId();
    @Nullable public abstract String body();
    @Nullable public abstract String subject();
    @Nullable public abstract Uri videoAttachment();
    public abstract UUID uuid();
    public abstract Status status();

    public static final String TABLE = "LocalMessage";

    public static final String ID = "_id";
    public static final String INSERTED_MESSAGE_ID = "insertedMessageId";
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

    static final String CREATE = Joiner.on('\n').join(
            "CREATE TABLE " + TABLE + "(",
                ID + " INTEGER NOT NULL PRIMARY KEY,",
                INSERTED_MESSAGE_ID + " BIG INTEGER NOT NULL DEFAULT 0,",
                ROOT_MESSAGE_ID + " BIG INTEGER NOT NULL,",
                PARENT_MESSAGE_ID + " BIG INTEGER NOT NULL,",
                GROUP_ID + " BIG INTEGER NOT NULL,",
                CATEGORY_ID + " BIG INTEGER NOT NULL,",
                THREAD_ID + " BIG INTEGER NOT NULL,",
                BODY + " TEXT,",
                SUBJECT + " TEXT,",
                VIDEO_ATTACHMENT + " TEXT,",
                UUID + " TEXT NOT NULL,",
                STATUS + " TEXT NOT NULL"
            + ")");


    static final String INDEX_ROOT_MESSAGE_ID = "idxRootMessageId";
    static final String INDEX_INSERTED_MESSAGE_ID = "idxInsertedMessageId";

    static final String CREATE_INDEX_ROOT_MESSAGE_ID =
            "CREATE INDEX " + INDEX_ROOT_MESSAGE_ID + " ON " + TABLE + " (" + ROOT_MESSAGE_ID + ")";

    static final String CREATE_INDEX_INSERTED_MESSAGE_ID =
            "CREATE INDEX " + INDEX_INSERTED_MESSAGE_ID + " ON " + TABLE + " (" + INSERTED_MESSAGE_ID + ")";

    public static Observable<List<LocalMessage>> getUnsentMessages(
            SqlBrite brite, long rootMessageId) {
        return brite.createQuery(TABLE, "SELECT * FROM " + TABLE + " WHERE rootMessageId = ?",
                String.valueOf(rootMessageId))
                .map(SqlBrite.Query::run)
                .map(READ_LIST);
    }

    public static Observable<LocalMessage> getMessage(SqlBrite brite, long messageId) {
        return brite.createQuery(TABLE, "SELECT * FROM " + TABLE + " WHERE _id = ?",
                String.valueOf(messageId))
                .map(SqlBrite.Query::run)
                .map(READ_SINGLE);
    }

    private static Func1<Cursor, List<LocalMessage>> READ_LIST = cursor -> {
        try {
            List<LocalMessage> list = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext())
                list.add(fromCursor(cursor));
            return list;
        } finally {
            cursor.close();
        }
    };

    private static final Func1<Cursor, LocalMessage> READ_SINGLE = cursor -> {
        List<LocalMessage> result = READ_LIST.call(cursor);
        return result.size() > 0 ? result.get(0) : null;
    };

    private static LocalMessage fromCursor(Cursor cursor) {
        Long id = cursor.getLong(cursor.getColumnIndexOrThrow(ID));
        Long insertedMessageId = cursor.getLong(cursor.getColumnIndexOrThrow(INSERTED_MESSAGE_ID));
        Long rootMessageId = cursor.getLong(cursor.getColumnIndexOrThrow(ROOT_MESSAGE_ID));
        Long groupId = cursor.getLong(cursor.getColumnIndexOrThrow(GROUP_ID));
        Long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(CATEGORY_ID));
        Long threadId = cursor.getLong(cursor.getColumnIndexOrThrow(THREAD_ID));
        Long parentMessageId = cursor.getLong(cursor.getColumnIndexOrThrow(PARENT_MESSAGE_ID));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(BODY));
        String subject = cursor.getString(cursor.getColumnIndexOrThrow(SUBJECT));
        Uri videoAttachment = nullableUri(
                cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_ATTACHMENT)));
        UUID uuid = java.util.UUID.fromString(
                cursor.getString(cursor.getColumnIndexOrThrow(UUID)));
        Status status = Enum.valueOf(
                Status.class, cursor.getString(cursor.getColumnIndexOrThrow(STATUS)));
        return new AutoParcel_LocalMessage(
                id, insertedMessageId, rootMessageId, groupId, categoryId, threadId,
                parentMessageId, body, subject, videoAttachment, uuid, status);
    }

    private static Uri nullableUri(String string) {
        return string == null ? null : Uri.parse(string);
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(ID, id);
            return this;
        }

        public Builder insertedMessageId(Long insertedMessageId) {
            if (insertedMessageId != null)
                values.put(INSERTED_MESSAGE_ID, insertedMessageId);
            else
                values.putNull(INSERTED_MESSAGE_ID);
            return this;
        }

        public Builder rootMessageId(long rootMessageId) {
            values.put(ROOT_MESSAGE_ID, rootMessageId);
            return this;
        }

        public Builder groupId(long groupId) {
            values.put(GROUP_ID, groupId);
            return this;
        }

        public Builder categoryId(long categoryId) {
            values.put(CATEGORY_ID, categoryId);
            return this;
        }

        public Builder threadId(long threadId) {
            values.put(THREAD_ID, threadId);
            return this;
        }

        public Builder parentMessageId(long parentMessageId) {
            values.put(PARENT_MESSAGE_ID, parentMessageId);
            return this;
        }

        public Builder body(String body) {
            if (body == null)
                values.putNull(BODY);
            else
                values.put(BODY, body);
            return this;
        }

        public Builder subject(String subject) {
            if (subject != null)
                values.put(SUBJECT, subject);
            else
                values.putNull(SUBJECT);
            return this;
        }

        public Builder videoAttachment(Uri videoAttachment) {
            if (videoAttachment != null)
                values.put(VIDEO_ATTACHMENT, videoAttachment.toString());
            else
                values.putNull(VIDEO_ATTACHMENT);
            return this;
        }

        public Builder uuid(UUID uuid) {
            values.put(UUID, uuid.toString());
            return this;
        }

        public Builder status(Status status) {
            values.put(STATUS, status.name());
            return this;
        }

        public ContentValues build() {
            return new ContentValues(values);
        }
    }
}

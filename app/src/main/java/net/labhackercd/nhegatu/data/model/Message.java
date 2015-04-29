package net.labhackercd.nhegatu.data.model;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * The message model used through the application.
 *
 * It can represent both "remote messages" and "local messages" (waiting for submission).
 * Local messages will have the messageId set to 0 and the createDate set to null, while
 * remote messages will have a non-null create date and a non-zero messageId.
 */
public interface Message extends Serializable {
    UUID getUuid();
    long getUserId();
    long getGroupId();
    long getCategoryId();
    long getThreadId();
    long getMessageId();
    long getRootMessageId();
    long getParentMessageId();
    @Nullable String getBody();
    @Nullable String getSubject();
    @Nullable Date getCreateDate();
}

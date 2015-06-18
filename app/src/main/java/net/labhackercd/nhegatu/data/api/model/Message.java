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

package net.labhackercd.nhegatu.data.api.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;
import auto.parcel.AutoParcel;
import net.labhackercd.nhegatu.data.api.model.util.JSON;
import net.labhackercd.nhegatu.data.api.model.util.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@AutoParcel
public abstract class Message implements net.labhackercd.nhegatu.data.model.Message, Parcelable, Serializable {

    public abstract int getStatus();
    public abstract boolean hasAttachments();
    @Nullable public abstract String getStatusByUserName();
    public abstract long getUserId();
    public abstract long getThreadId();
    @Nullable public abstract String getSubject();
    public abstract boolean isAnswer();
    @Nullable public abstract UUID getUuid();
    public abstract long getCompanyId();
    public abstract Date getCreateDate();
    @Nullable public abstract String getFormat();
    public abstract double getPriority();
    public abstract long getStatusByUserId();
    @Nullable public abstract Date getStatusDate();
    public abstract long getCategoryId();
    @Nullable public abstract String getBody();
    public abstract long getClassPK();
    public abstract boolean allowPingbacks();
    public abstract long getClassNameId();
    public abstract long getRootMessageId();
    public abstract long getParentMessageId();
    @Nullable public abstract Date getModifiedDate();
    public abstract boolean isAnonymous();
    public abstract long getGroupId();
    @Nullable public abstract String getUserName();
    public abstract long getMessageId();

    public static final JSONReader<Message> JSON_READER = new JSONReader<Message>() {
        @Override
        public Message fromJSON(JSONObject json) throws JSONException {
            int status = json.getInt("status");
            boolean attachments = json.getBoolean("attachments");
            String statusByUserName = json.getString("statusByUserName");
            long userId = json.getLong("userId");
            long threadId = json.getLong("threadId");
            String subject = json.getString("subject");
            boolean answer = json.getBoolean("answer");
            String uuid = json.getString("uuid");
            long companyId = json.getLong("companyId");
            Date createDate = JSON.getJSONLongAsDate(json, "createDate");
            String format = json.getString("format");
            double priority = json.getDouble("priority");
            long statusByUserId = json.getLong("statusByUserId");
            Date statusDate = JSON.getJSONLongAsDate(json, "statusDate");
            long categoryId = json.getLong("categoryId");
            String body = json.getString("body");
            long classPK = json.getLong("classPK");
            boolean allowPingbacks = json.getBoolean("allowPingbacks");
            long classNameId = json.getLong("classNameId");
            long rootMessageId = json.getLong("rootMessageId");
            long parentMessageId = json.getLong("parentMessageId");
            Date modifiedDate = JSON.getJSONLongAsDate(json, "modifiedDate");
            boolean anonymous = json.getBoolean("anonymous");
            long groupId = json.getLong("groupId");
            String userName = json.getString("userName");
            long messageId = json.getLong("messageId");
            return new AutoParcel_Message(
                    status, attachments, statusByUserName, userId, threadId, subject,
                    answer, UUID.fromString(uuid), companyId, createDate, format, priority,
                    statusByUserId, statusDate, categoryId, body, classPK, allowPingbacks,
                    classNameId, rootMessageId, parentMessageId, modifiedDate, anonymous,
                    groupId, userName, messageId);
        }
    };
}

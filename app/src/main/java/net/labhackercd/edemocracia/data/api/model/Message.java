package net.labhackercd.edemocracia.data.api.model;

import net.labhackercd.edemocracia.data.api.model.util.JSON;
import net.labhackercd.edemocracia.data.api.model.util.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Message extends BaseModel {

    private int status;
    private boolean attachments;
    private String statusByUserName;
    private long userId;
    private long threadId;
    private String subject;
    private boolean answer;
    private String uuid;
    private long companyId;
    private Date createDate;
    private String format;
    private double priority;
    private long statusByUserId;
    private Date statusDate;
    private long categoryId;
    private String body;
    private long classPK;
    private boolean allowPingbacks;
    private long classNameId;
    private long rootMessageId;
    private long parentMessageId;
    private Date modifiedDate;
    private boolean anonymous;
    private long groupId;
    private String userName;
    private long messageId;

    public int getStatus() {
        return status;
    }

    public boolean isAttachments() {
        return attachments;
    }

    public String getStatusByUserName() {
        return statusByUserName;
    }

    public long getUserId() {
        return userId;
    }

    public long getThreadId() {
        return threadId;
    }

    public String getSubject() {
        return subject;
    }

    public boolean isAnswer() {
        return answer;
    }

    public String getUuid() {
        return uuid;
    }

    public long getCompanyId() {
        return companyId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getFormat() {
        return format;
    }

    public double getPriority() {
        return priority;
    }

    public long getStatusByUserId() {
        return statusByUserId;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public String getBody() {
        return body;
    }

    public long getClassPK() {
        return classPK;
    }

    public boolean allowPingbacks() {
        return allowPingbacks;
    }

    public long getClassNameId() {
        return classNameId;
    }

    public long getRootMessageId() {
        return rootMessageId;
    }

    public long getParentMessageId() {
        return parentMessageId;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getUserName() {
        return userName;
    }

    public long getMessageId() {
        return messageId;
    }

    public static final JSONReader<Message> JSON_READER = new JSONReader<Message>() {
        @Override
        public Message fromJSON(JSONObject json) throws JSONException {
            Message instance = new Message();

            instance.status = json.getInt("status");
            instance.attachments = json.getBoolean("attachments");
            instance.statusByUserName = json.getString("statusByUserName");
            instance.userId = json.getLong("userId");
            instance.threadId = json.getLong("threadId");
            instance.subject = json.getString("subject");
            instance.answer = json.getBoolean("answer");
            instance.uuid = json.getString("uuid");
            instance.companyId = json.getLong("companyId");
            instance.createDate = JSON.getJSONLongAsDate(json, "createDate");
            instance.format = json.getString("format");
            instance.priority = json.getDouble("priority");
            instance.statusByUserId = json.getLong("statusByUserId");
            instance.statusDate = JSON.getJSONLongAsDate(json, "statusDate");
            instance.categoryId = json.getLong("categoryId");
            instance.body = json.getString("body");
            instance.classPK = json.getLong("classPK");
            instance.allowPingbacks = json.getBoolean("allowPingbacks");
            instance.classNameId = json.getLong("classNameId");
            instance.rootMessageId = json.getLong("rootMessageId");
            instance.parentMessageId = json.getLong("parentMessageId");
            instance.modifiedDate = JSON.getJSONLongAsDate(json, "modifiedDate");
            instance.anonymous = json.getBoolean("anonymous");
            instance.groupId = json.getLong("groupId");
            instance.userName = json.getString("userName");
            instance.messageId = json.getLong("messageId");

            return instance;
        }
    };
}

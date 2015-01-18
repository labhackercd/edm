package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Message extends Content {

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

    public static Message fromJSONObject(JSONObject obj) throws JSONException {
        Message instance = new Message();

        instance.status = obj.getInt("status");
        instance.attachments = obj.getBoolean("attachments");
        instance.statusByUserName = obj.getString("statusByUserName");
        instance.userId = obj.getLong("userId");
        instance.threadId = obj.getLong("threadId");
        instance.subject = obj.getString("subject");
        instance.answer = obj.getBoolean("answer");
        instance.uuid = obj.getString("uuid");
        instance.companyId = obj.getLong("companyId");
        instance.createDate = new Date(obj.getLong("createDate"));
        instance.format = obj.getString("format");
        instance.priority = obj.getDouble("priority");
        instance.statusByUserId = obj.getLong("statusByUserId");
        instance.statusDate = new Date(obj.getLong("statusDate"));
        instance.categoryId = obj.getLong("categoryId");
        instance.body = obj.getString("body");
        instance.classPK = obj.getLong("classPK");
        instance.allowPingbacks = obj.getBoolean("allowPingbacks");
        instance.classNameId = obj.getLong("classNameId");
        instance.rootMessageId = obj.getLong("rootMessageId");
        instance.parentMessageId = obj.getLong("parentMessageId");
        instance.modifiedDate = new Date(obj.getLong("modifiedDate"));
        instance.anonymous = obj.getBoolean("anonymous");
        instance.groupId = obj.getLong("groupId");
        instance.userName = obj.getString("userName");
        instance.messageId = obj.getLong("messageId");

        return instance;
    }

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

    public boolean isAllowPingbacks() {
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

    @Override
    public long getId() {
        return getMessageId();
    }
}
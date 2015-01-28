package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import br.leg.camara.labhacker.edemocracia.util.SimpleArrayAdapter;
import br.leg.camara.labhacker.edemocracia.util.GsonParcelable;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;

public class Message extends GsonParcelable implements SimpleArrayAdapter.Identifiable {

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

    public static Message create(Thread thread, String subject, String body, String format,
                                 boolean anonymous, double priority, boolean allowPingbacks) {
        return create(
                thread.getGroupId(), thread.getCategoryId(), thread.getThreadId(),
                thread.getRootMessageId(), subject, body, format, anonymous, priority, allowPingbacks);
    }

    public static Message create(
        long groupId, long categoryId, long threadId, long parentMessageId, String subject,
        String body, String format, boolean anonymous, double priority, boolean allowPingbacks) {

        Message r = new Message();

        r.groupId = groupId;
        r.categoryId = categoryId;
        r.threadId = threadId;
        r.parentMessageId = parentMessageId;
        r.subject = subject;
        r.body = body;
        r.format = format;
        r.anonymous = anonymous;
        r.priority = priority;
        r.allowPingbacks = allowPingbacks;

        return r;
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

    @Override
    public long getId() {
        return getMessageId();
    }

    @Override
    public String toString() {
        return getSubject();
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
            instance.createDate = new Date(json.getLong("createDate"));
            instance.format = json.getString("format");
            instance.priority = json.getDouble("priority");
            instance.statusByUserId = json.getLong("statusByUserId");
            instance.statusDate = new Date(json.getLong("statusDate"));
            instance.categoryId = json.getLong("categoryId");
            instance.body = json.getString("body");
            instance.classPK = json.getLong("classPK");
            instance.allowPingbacks = json.getBoolean("allowPingbacks");
            instance.classNameId = json.getLong("classNameId");
            instance.rootMessageId = json.getLong("rootMessageId");
            instance.parentMessageId = json.getLong("parentMessageId");
            instance.modifiedDate = new Date(json.getLong("modifiedDate"));
            instance.anonymous = json.getBoolean("anonymous");
            instance.groupId = json.getLong("groupId");
            instance.userName = json.getString("userName");
            instance.messageId = json.getLong("messageId");

            return instance;
        }
    };
}
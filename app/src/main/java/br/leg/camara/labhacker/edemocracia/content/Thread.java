package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Thread extends Content {

    private int status;
    private int viewCount;
    private int messageCount;
    private Date lastPostDate;
    private long companyId;
    private long statusByUserId;
    private long rootMessageUserId;
    private long rootMessageId;
    private boolean question;
    private long lastPostByUserId;
    private int priority;
    private long threadId;
    private long groupId;
    private String statusByUserName;
    private Date statusDate;
    private long categoryId;

    public static Thread fromJSONObject(JSONObject obj) throws JSONException {
        Thread instance = new Thread();

        instance.status = obj.getInt("status");
        instance.viewCount = obj.getInt("viewCount");
        instance.messageCount = obj.getInt("messageCount");
        instance.lastPostDate = new Date(obj.getLong("lastPostDate"));
        instance.companyId = obj.getLong("companyId");
        instance.statusByUserId = obj.getLong("statusByUserId");
        instance.rootMessageUserId = obj.getLong("rootMessageUserId");
        instance.rootMessageId = obj.getLong("rootMessageId");
        instance.question = obj.getBoolean("question");
        instance.lastPostByUserId = obj.getLong("lastPostByUserId");
        instance.priority = obj.getInt("priority");
        instance.threadId = obj.getLong("threadId");
        instance.groupId = obj.getLong("groupId");
        instance.statusByUserName = obj.getString("statusByUserName");
        instance.statusDate = new Date(obj.getLong("statusDate"));
        instance.categoryId = obj.getLong("categoryId");

        return instance;
    }

    public int getStatus() {
        return status;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public Date getLastPostDate() {
        return lastPostDate;
    }

    public long getCompanyId() {
        return companyId;
    }

    public long getStatusByUserId() {
        return statusByUserId;
    }

    public long getRootMessageUserId() {
        return rootMessageUserId;
    }

    public long getRootMessageId() {
        return rootMessageId;
    }

    public boolean isQuestion() {
        return question;
    }

    public long getLastPostByUserId() {
        return lastPostByUserId;
    }

    public int getPriority() {
        return priority;
    }

    public long getThreadId() {
        return threadId;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getStatusByUserName() {
        return statusByUserName;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public long getCategoryId() {
        return categoryId;
    }

    @Override
    public long getId() {
        return getThreadId();
    }
}
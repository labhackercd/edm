package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import br.leg.camara.labhacker.edemocracia.util.JSONReader;

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

    public static final JSONReader<Thread> JSON_READER = new JSONReader<Thread>() {
        @Override
        public Thread fromJSON(JSONObject json) throws JSONException {
            Thread instance = new Thread();

            instance.status = json.getInt("status");
            instance.viewCount = json.getInt("viewCount");
            instance.messageCount = json.getInt("messageCount");
            instance.lastPostDate = new Date(json.getLong("lastPostDate"));
            instance.companyId = json.getLong("companyId");
            instance.statusByUserId = json.getLong("statusByUserId");
            instance.rootMessageUserId = json.getLong("rootMessageUserId");
            instance.rootMessageId = json.getLong("rootMessageId");
            instance.question = json.getBoolean("question");
            instance.lastPostByUserId = json.getLong("lastPostByUserId");
            instance.priority = json.getInt("priority");
            instance.threadId = json.getLong("threadId");
            instance.groupId = json.getLong("groupId");
            instance.statusByUserName = json.getString("statusByUserName");
            instance.statusDate = new Date(json.getLong("statusDate"));
            instance.categoryId = json.getLong("categoryId");

            return instance;
        }
    };
}
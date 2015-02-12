package net.labhackercd.edemocracia.data.model;

import android.net.Uri;

import java.util.Date;

public class Thread extends GsonParcelable {

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
    private Message rootMessage = null;

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

    public Message getRootMessage() {
        return rootMessage;
    }

    // FIXME Maybe we should move this *set root message* method into some other layer?
    public void setRootMessage(Message message) {
        rootMessage = message;
    }

    public String getSubject() {
        Message rootMessage = getRootMessage();
        return rootMessage == null ? null : rootMessage.getSubject();
    }

    @Override
    public String toString() {
        String subject = getSubject();
        if (subject == null) {
            subject = super.toString();
        }
        return subject;
    }

    public Uri getUserPortrait() {
        Uri portrait = null;
        Message root = getRootMessage();
        if (root != null) {
            portrait = root.getUserPortrait();
        }
        return portrait;
    }
}
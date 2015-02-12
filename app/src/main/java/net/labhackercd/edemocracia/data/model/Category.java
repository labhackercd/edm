package net.labhackercd.edemocracia.data.model;

import android.net.Uri;

import java.util.Date;

public class Category extends GsonParcelable implements Forum {

    private String userName;
    private String description;
    private Date lastPostDate;
    private long companyId;
    private Date createDate;
    private long parentCategoryId;
    private long userId;
    private String uuid;
    private int threadCount;
    private long categoryId;
    private Date modifiedDate;
    private long groupId;
    private int messageCount;
    private String displayStyle;
    private String name;


    public String getUserName() {
        return userName;
    }

    public String getDescription() {
        return description;
    }

    public Date getLastPostDate() {
        return lastPostDate;
    }

    public long getCompanyId() {
        return companyId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public long getParentCategoryId() {
        return parentCategoryId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUuid() {
        return uuid;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public long getGroupId() {
        return groupId;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getDisplayStyle() {
        return displayStyle;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Uri getUserPortrait() {
        return null;
    }
}

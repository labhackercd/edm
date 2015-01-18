package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Category extends Content {

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

    public static Category fromJSONObject(JSONObject obj) throws JSONException {
        Category instance = new Category();

        instance.userName = obj.getString("userName");
        instance.description = obj.getString("description");
        instance.lastPostDate = new Date(obj.getLong("lastPostDate"));
        instance.companyId = obj.getLong("companyId");
        instance.createDate = new Date(obj.getLong("createDate"));
        instance.parentCategoryId = obj.getLong("parentCategoryId");
        instance.userId = obj.getLong("userId");
        instance.uuid = obj.getString("uuid");
        instance.threadCount = obj.getInt("threadCount");
        instance.categoryId = obj.getLong("categoryId");
        instance.modifiedDate = new Date(obj.getLong("modifiedDate"));
        instance.groupId = obj.getLong("groupId");
        instance.messageCount = obj.getInt("messageCount");
        instance.displayStyle = obj.getString("displayStyle");
        instance.name = obj.getString("name");

        return instance;
    }

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
    public long getId() {
        return getCategoryId();
    }
}

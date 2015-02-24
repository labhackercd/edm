package net.labhackercd.edemocracia.data.api.model;

import net.labhackercd.edemocracia.data.api.model.util.JSON;
import net.labhackercd.edemocracia.data.api.model.util.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Category extends BaseModel {

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

    public static final JSONReader<Category> JSON_READER = new JSONReader<Category>() {
        @Override
        public Category fromJSON(JSONObject json) throws JSONException {
            Category instance = new Category();

            instance.userName = json.getString("userName");
            instance.description = json.getString("description");
            instance.lastPostDate = JSON.getJSONLongAsDate(json, "lastPostDate");
            instance.companyId = json.getLong("companyId");
            instance.createDate = JSON.getJSONLongAsDate(json, "createDate");
            instance.parentCategoryId = json.getLong("parentCategoryId");
            instance.userId = json.getLong("userId");
            instance.uuid = json.getString("uuid");
            instance.threadCount = json.getInt("threadCount");
            instance.categoryId = json.getLong("categoryId");
            instance.modifiedDate = JSON.getJSONLongAsDate(json, "modifiedDate");
            instance.groupId = json.getLong("groupId");
            instance.messageCount = json.getInt("messageCount");
            instance.displayStyle = json.getString("displayStyle");
            instance.name = json.getString("name");

            return instance;
        }
    };
}

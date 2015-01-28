package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import br.leg.camara.labhacker.edemocracia.util.GsonParcelable;
import br.leg.camara.labhacker.edemocracia.util.Identifiable;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;

public class Category extends GsonParcelable implements Forum, Identifiable {

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
    public long getId() {
        return getCategoryId();
    }

    @Override
    public String toString() {
        return getName();
    }

    public static final JSONReader<Category> JSON_READER = new JSONReader<Category>() {
        @Override
        public Category fromJSON(JSONObject json) throws JSONException {
            Category instance = new Category();

            instance.userName = json.getString("userName");
            instance.description = json.getString("description");
            instance.companyId = json.getLong("companyId");
            instance.createDate = new Date(json.getLong("createDate"));
            instance.parentCategoryId = json.getLong("parentCategoryId");
            instance.userId = json.getLong("userId");
            instance.uuid = json.getString("uuid");
            instance.threadCount = json.getInt("threadCount");
            instance.categoryId = json.getLong("categoryId");
            instance.modifiedDate = new Date(json.getLong("modifiedDate"));
            instance.groupId = json.getLong("groupId");
            instance.messageCount = json.getInt("messageCount");
            instance.displayStyle = json.getString("displayStyle");
            instance.name = json.getString("name");


            if (json.isNull("lastPostDate")) {
                instance.lastPostDate = null;
            } else {
                instance.lastPostDate = new Date(json.getLong("lastPostDate"));
            }

            return instance;
        }
    };
}

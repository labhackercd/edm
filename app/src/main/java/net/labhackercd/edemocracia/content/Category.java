package net.labhackercd.edemocracia.content;

import android.net.Uri;

import net.labhackercd.edemocracia.util.EDMSession;
import net.labhackercd.edemocracia.util.GsonParcelable;
import net.labhackercd.edemocracia.util.Identifiable;
import net.labhackercd.edemocracia.util.JSONReader;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

public class Category extends GsonParcelable implements Forum, Identifiable {

    private String userName;
    private String description;
    private DateTime lastPostDate;
    private long companyId;
    private DateTime createDate;
    private long parentCategoryId;
    private long userId;
    private String uuid;
    private int threadCount;
    private long categoryId;
    private DateTime modifiedDate;
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

    public DateTime getLastPostDate() {
        return lastPostDate;
    }

    public long getCompanyId() {
        return companyId;
    }

    public DateTime getCreateDate() {
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

    public DateTime getModifiedDate() {
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

    public Uri getIconUri() {
        return Uri.parse(EDMSession.SERVICE_URL + "/image/user_male_portrait?img_id=" + getUserId());
    }

    public static final JSONReader<Category> JSON_READER = new JSONReader<Category>() {
        @Override
        public Category fromJSON(JSONObject json) throws JSONException {
            Category instance = new Category();

            instance.userName = json.getString("userName");
            instance.description = json.getString("description");
            instance.companyId = json.getLong("companyId");
            instance.createDate = new DateTime(json.getLong("createDate"));
            instance.parentCategoryId = json.getLong("parentCategoryId");
            instance.userId = json.getLong("userId");
            instance.uuid = json.getString("uuid");
            instance.threadCount = json.getInt("threadCount");
            instance.categoryId = json.getLong("categoryId");
            instance.modifiedDate = new DateTime(json.getLong("modifiedDate"));
            instance.groupId = json.getLong("groupId");
            instance.messageCount = json.getInt("messageCount");
            instance.displayStyle = json.getString("displayStyle");
            instance.name = json.getString("name");


            if (json.isNull("lastPostDate")) {
                instance.lastPostDate = null;
            } else {
                instance.lastPostDate = new DateTime(json.getLong("lastPostDate"));
            }

            return instance;
        }
    };
}

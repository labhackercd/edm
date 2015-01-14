package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Category {

    private int categoryId;
    private int companyId;
    private Date createDate;
    private String description;
    private String displayStyle;
    private int groupId;
    private Date lastPostDate;
    private int messageCount;
    private Date modifiedDate;
    private String name;
    private int parentCategoryId;
    private int threadCount;
    private int userId;
    private String userName;

    /* {
        "categoryId": 1937800,
        "companyId": 10131,
        "createDate": 1412194712700,
        "description": "",
        "displayStyle": "default",
        "groupId": 1927974,
        "lastPostDate": 1412194842497,
        "messageCount": 1,
        "modifiedDate": 1412194712700,
        "name": "0. Lista de discussão para formação de grupos",
        "parentCategoryId": 0,
        "threadCount": 1,
        "userId": 12777,
        "userName": "Equipe e-Democracia",
        "uuid": "49539a73-5eb3-4c2f-9e22-4ea9b99176e0"
    } */
    public static Category fromJSONObject(JSONObject o) throws JSONException {
        Category r = new Category();

        r.categoryId = o.getInt("categoryId");
        r.companyId = o.getInt("companyId");
        // TODO r.createDate = o.getDate("createDate");
        r.description = o.getString("description");
        r.displayStyle = o.getString("displayStyle");
        r.groupId = o.getInt("groupId");
        // TODO r.lastPostDate = o.getDate("lastPostDate");
        r.messageCount = o.getInt("messageCount");
        // TODO r.modifiedDate = o.getDate("modifiedDate");
        r.name = o.getString("name");
        r.parentCategoryId = o.getInt("parentCategoryId");
        r.threadCount = o.getInt("threadCount");
        r.userId = o.getInt("userId");
        r.userName = o.getString("userName");

        return r;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayStyle() {
        return displayStyle;
    }

    public Date getLastPostDate() {
        return lastPostDate;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getName() {
        return name;
    }

    public int getParentCategoryId() {
        return parentCategoryId;
    }

    public int getThreadCount() {
        return threadCount;
    }
}

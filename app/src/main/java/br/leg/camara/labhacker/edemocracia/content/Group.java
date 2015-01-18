package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;


public class Group extends Content {

    private String friendlyURL;
    private long classPK;
    private String description;
    private long creatorUserId;
    private long classNameId;
    private long companyId;
    private boolean site;
    private String typeSettings;
    private long parentGroupId;
    private boolean active;
    private long liveGroupId;
    private int type;
    private long groupId;
    private String name;

    public static Group fromJSONObject(JSONObject obj) throws JSONException {
        Group instance = new Group();

        instance.friendlyURL = obj.getString("friendlyURL");
        instance.classPK = obj.getLong("classPK");
        instance.description = obj.getString("description");
        instance.creatorUserId = obj.getLong("creatorUserId");
        instance.classNameId = obj.getLong("classNameId");
        instance.companyId = obj.getLong("companyId");
        instance.site = obj.getBoolean("site");
        instance.typeSettings = obj.getString("typeSettings");
        instance.parentGroupId = obj.getLong("parentGroupId");
        instance.active = obj.getBoolean("active");
        instance.liveGroupId = obj.getLong("liveGroupId");
        instance.type = obj.getInt("type");
        instance.groupId = obj.getLong("groupId");
        instance.name = obj.getString("name");

        return instance;
    }

    public String getFriendlyURL() {
        return friendlyURL;
    }

    public long getClassPK() {
        return classPK;
    }

    public String getDescription() {
        return description;
    }

    public long getCreatorUserId() {
        return creatorUserId;
    }

    public long getClassNameId() {
        return classNameId;
    }

    public long getCompanyId() {
        return companyId;
    }

    public boolean isSite() {
        return site;
    }

    public String getTypeSettings() {
        return typeSettings;
    }

    public long getParentGroupId() {
        return parentGroupId;
    }

    public boolean isActive() {
        return active;
    }

    public long getLiveGroupId() {
        return liveGroupId;
    }

    public int getType() {
        return type;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return getGroupId();
    }
}
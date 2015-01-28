package br.leg.camara.labhacker.edemocracia.content;

import org.json.JSONException;
import org.json.JSONObject;

import br.leg.camara.labhacker.edemocracia.util.Identifiable;
import br.leg.camara.labhacker.edemocracia.util.GsonParcelable;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;


public class Group extends GsonParcelable implements Identifiable {

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

    @Override
    public String toString() {
        return getName();
    }

    public static final JSONReader<Group> JSON_READER = new JSONReader<Group>() {
        @Override
        public Group fromJSON(JSONObject json) throws JSONException {
            Group instance = new Group();

            instance.friendlyURL = json.getString("friendlyURL");
            instance.classPK = json.getLong("classPK");
            instance.description = json.getString("description");
            instance.creatorUserId = json.getLong("creatorUserId");
            instance.classNameId = json.getLong("classNameId");
            instance.companyId = json.getLong("companyId");
            instance.site = json.getBoolean("site");
            instance.typeSettings = json.getString("typeSettings");
            instance.parentGroupId = json.getLong("parentGroupId");
            instance.active = json.getBoolean("active");
            instance.liveGroupId = json.getLong("liveGroupId");
            instance.type = json.getInt("type");
            instance.groupId = json.getLong("groupId");
            instance.name = json.getString("name");

            return instance;
        }
    };
}

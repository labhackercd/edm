package net.labhackercd.edemocracia.data.model;

import android.net.Uri;

import net.labhackercd.edemocracia.data.api.GroupService;

public class Group extends GsonParcelable implements Forum {

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

    @Override
    public long getCategoryId() {
        // Groups aren't categories
        return -1;
    }

    public String getName() {
        return name;
    }

    public Uri getGroupImage() {
        return Uri.parse(GroupService.SERVICE_URL + "/documents/" + getGroupId() + "/0/icone");
    }
}

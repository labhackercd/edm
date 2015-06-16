/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.data.api.model;

import net.labhackercd.nhegatu.data.api.model.util.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Group implements Serializable {

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
    private boolean closed;

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

    public boolean isClosed() {
        return closed;
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
            instance.closed = json.getBoolean("closed");

            return instance;
        }
    };
}

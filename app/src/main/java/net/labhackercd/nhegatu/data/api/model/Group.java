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

import android.os.Parcelable;
import android.support.annotation.Nullable;
import auto.parcel.AutoParcel;
import net.labhackercd.nhegatu.data.api.model.util.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@AutoParcel
public abstract class Group implements Parcelable, Serializable {

    @Nullable public abstract String getFriendlyURL();
    public abstract long getClassPK();
    @Nullable public abstract String getDescription();
    public abstract long getCreatorUserId();
    public abstract long getClassNameId();
    public abstract long getCompanyId();
    public abstract boolean isSite();
    @Nullable public abstract String getTypeSettings();
    public abstract long getParentGroupId();
    public abstract boolean isActive();
    public abstract long getLiveGroupId();
    public abstract int getType();
    public abstract long getGroupId();
    @Nullable public abstract String getName();
    public abstract boolean isClosed();
    public abstract boolean isWebOnly();
    public abstract int getPriority();

    public static final JSONReader<Group> JSON_READER = new JSONReader<Group>() {
        @Override
        public Group fromJSON(JSONObject json) throws JSONException {
            String friendlyURL = json.getString("friendlyURL");
            long classPK = json.getLong("classPK");
            String description = json.getString("description");
            long creatorUserId = json.getLong("creatorUserId");
            long classNameId = json.getLong("classNameId");
            long companyId = json.getLong("companyId");
            boolean site = json.getBoolean("site");
            String typeSettings = json.getString("typeSettings");
            long parentGroupId = json.getLong("parentGroupId");
            boolean active = json.getBoolean("active");
            long liveGroupId = json.getLong("liveGroupId");
            int type = json.getInt("type");
            long groupId = json.getLong("groupId");
            String name = json.getString("name");

            // Extra fields.
            boolean closed = json.getBoolean("closed");
            boolean webOnly = !json.getBoolean("notWebOnly");
            int priority = json.getInt("priority");

            return new AutoParcel_Group(
                    friendlyURL, classPK, description, creatorUserId, classNameId,
                    companyId, site, typeSettings, parentGroupId, active, liveGroupId,
                    type, groupId, name, closed, webOnly, priority);
        }
    };
}

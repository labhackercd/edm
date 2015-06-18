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
import net.labhackercd.nhegatu.data.api.model.util.JSON;
import net.labhackercd.nhegatu.data.api.model.util.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@AutoParcel
public abstract class Category implements Parcelable, Serializable {

    @Nullable public abstract String getUserName();
    @Nullable public abstract String getDescription();
    @Nullable public abstract Date getLastPostDate();
    public abstract long getCompanyId();
    @Nullable public abstract Date getCreateDate();
    public abstract long getParentCategoryId();
    public abstract long getUserId();
    @Nullable public abstract UUID getUuid();
    public abstract int getThreadCount();
    public abstract long getCategoryId();
    @Nullable public abstract Date getModifiedDate();
    public abstract long getGroupId();
    public abstract int getMessageCount();
    @Nullable public abstract String getDisplayStyle();
    @Nullable public abstract String getName();

    public static final JSONReader<Category> JSON_READER = new JSONReader<Category>() {
        @Override
        public Category fromJSON(JSONObject json) throws JSONException {
            String userName = json.getString("userName");
            String description = json.getString("description");
            Date lastPostDate = JSON.getJSONLongAsDate(json, "lastPostDate");
            long companyId = json.getLong("companyId");
            Date createDate = JSON.getJSONLongAsDate(json, "createDate");
            long parentCategoryId = json.getLong("parentCategoryId");
            long userId = json.getLong("userId");
            String uuid = json.getString("uuid");
            int threadCount = json.getInt("threadCount");
            long categoryId = json.getLong("categoryId");
            Date modifiedDate = JSON.getJSONLongAsDate(json, "modifiedDate");
            long groupId = json.getLong("groupId");
            int messageCount = json.getInt("messageCount");
            String displayStyle = json.getString("displayStyle");
            String name = json.getString("name");
            return new AutoParcel_Category(
                    userName, description, lastPostDate, companyId, createDate,
                    parentCategoryId, userId, UUID.fromString(uuid), threadCount,
                    categoryId, modifiedDate, groupId, messageCount, displayStyle, name);
        }
    };
}

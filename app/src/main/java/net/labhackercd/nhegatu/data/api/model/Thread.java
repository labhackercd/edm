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

@AutoParcel
public abstract class Thread implements Parcelable, Serializable {

    public abstract int getStatus();
    public abstract int getViewCount();
    public abstract int getMessageCount();
    @Nullable public abstract Date getLastPostDate();
    public abstract long getCompanyId();
    public abstract long getStatusByUserId();
    public abstract long getRootMessageUserId();
    public abstract long getRootMessageId();
    public abstract boolean isQuestion();
    public abstract long getLastPostByUserId();
    public abstract int getPriority();
    public abstract long getThreadId();
    public abstract long getGroupId();
    @Nullable public abstract String getStatusByUserName();
    public abstract Date getStatusDate();
    public abstract long getCategoryId();

    public static final JSONReader<Thread> JSON_READER = new JSONReader<Thread>() {
        @Override
        public Thread fromJSON(JSONObject json) throws JSONException {
            int status = json.getInt("status");
            int viewCount = json.getInt("viewCount");
            int messageCount = json.getInt("messageCount");
            Date lastPostDate = JSON.getJSONLongAsDate(json, "lastPostDate");
            long companyId = json.getLong("companyId");
            long statusByUserId = json.getLong("statusByUserId");
            long rootMessageUserId = json.getLong("rootMessageUserId");
            long rootMessageId = json.getLong("rootMessageId");
            boolean question = json.getBoolean("question");
            long lastPostByUserId = json.getLong("lastPostByUserId");
            int priority = json.getInt("priority");
            long threadId = json.getLong("threadId");
            long groupId = json.getLong("groupId");
            String statusByUserName = json.getString("statusByUserName");
            Date statusDate = JSON.getJSONLongAsDate(json, "statusDate");
            long categoryId = json.getLong("categoryId");
            return new AutoParcel_Thread(
                    status, viewCount, messageCount, lastPostDate, companyId,
                    statusByUserId, rootMessageUserId, rootMessageId, question,
                    lastPostByUserId, priority, threadId, groupId, statusByUserName,
                    statusDate, categoryId);
        }
    };
}
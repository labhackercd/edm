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

import net.labhackercd.nhegatu.data.api.model.util.JSON;
import net.labhackercd.nhegatu.data.api.model.util.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Thread implements Serializable {

    private int status;
    private int viewCount;
    private int messageCount;
    private Date lastPostDate;
    private long companyId;
    private long statusByUserId;
    private long rootMessageUserId;
    private long rootMessageId;
    private boolean question;
    private long lastPostByUserId;
    private int priority;
    private long threadId;
    private long groupId;
    private String statusByUserName;
    private Date statusDate;
    private long categoryId;

    public int getStatus() {
        return status;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public Date getLastPostDate() {
        return lastPostDate;
    }

    public long getCompanyId() {
        return companyId;
    }

    public long getStatusByUserId() {
        return statusByUserId;
    }

    public long getRootMessageUserId() {
        return rootMessageUserId;
    }

    public long getRootMessageId() {
        return rootMessageId;
    }

    public boolean isQuestion() {
        return question;
    }

    public long getLastPostByUserId() {
        return lastPostByUserId;
    }

    public int getPriority() {
        return priority;
    }

    public long getThreadId() {
        return threadId;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getStatusByUserName() {
        return statusByUserName;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public static final JSONReader<Thread> JSON_READER = new JSONReader<Thread>() {
        @Override
        public Thread fromJSON(JSONObject json) throws JSONException {
            Thread instance = new Thread();

            instance.status = json.getInt("status");
            instance.viewCount = json.getInt("viewCount");
            instance.messageCount = json.getInt("messageCount");
            instance.lastPostDate = JSON.getJSONLongAsDate(json, "lastPostDate");
            instance.companyId = json.getLong("companyId");
            instance.statusByUserId = json.getLong("statusByUserId");
            instance.rootMessageUserId = json.getLong("rootMessageUserId");
            instance.rootMessageId = json.getLong("rootMessageId");
            instance.question = json.getBoolean("question");
            instance.lastPostByUserId = json.getLong("lastPostByUserId");
            instance.priority = json.getInt("priority");
            instance.threadId = json.getLong("threadId");
            instance.groupId = json.getLong("groupId");
            instance.statusByUserName = json.getString("statusByUserName");
            instance.statusDate = JSON.getJSONLongAsDate(json, "statusDate");
            instance.categoryId = json.getLong("categoryId");

            return instance;
        }
    };
}
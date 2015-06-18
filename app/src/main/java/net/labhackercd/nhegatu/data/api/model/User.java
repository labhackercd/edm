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
import java.util.Date;
import java.util.UUID;

import static net.labhackercd.nhegatu.data.api.model.util.JSON.getJSONLongAsDate;

@AutoParcel
public abstract class User implements Parcelable, Serializable {

    public abstract boolean hasAgreedToTermsOfUse();
    @Nullable public abstract String getComments();
    public abstract long getCompanyId();
    public abstract long getContactId();
    @Nullable public abstract Date getCreateDate();
    public abstract boolean isDefaultUser();
    @Nullable public abstract String getEmailAddress();
    public abstract boolean isEmailAddressVerified();
    public abstract long getFacebookId();
    public abstract int getFailedLoginAttempts();
    @Nullable public abstract String getFirstName();
    public abstract int getGraceLoginCount();
    @Nullable public abstract String getGreeting();
    @Nullable public abstract String getJobTitle();
    @Nullable public abstract String getLanguageId();
    @Nullable public abstract Date getLastFailedLoginDate();
    @Nullable public abstract Date getLastLoginDate();
    @Nullable public abstract String getLastLoginIP();
    @Nullable public abstract String getLastName();
    public abstract boolean isLockout();
    @Nullable public abstract Date getLockoutDate();
    @Nullable public abstract Date getLoginDate();
    @Nullable public abstract String getLoginIP();
    @Nullable public abstract String getMiddleName();
    @Nullable public abstract Date getModifiedDate();
    @Nullable public abstract String getOpenId();
    public abstract long getPortraitId();
    @Nullable public abstract String getReminderQueryAnswer();
    @Nullable public abstract String getReminderQueryQuestion();
    @Nullable public abstract String getScreenName();
    public abstract int getStatus();
    @Nullable public abstract String getTimeZoneId();
    public abstract long getUserId();
    public abstract UUID getUUID();

    public static final JSONReader<User> JSON_READER = new JSONReader<User>() {
        @Override
        public User fromJSON(JSONObject json) throws JSONException {
            boolean agreedToTermsOfUse = json.getBoolean("agreedToTermsOfUse");
            String comments = json.getString("comments");
            long companyId = json.getLong("companyId");
            long contactId = json.getLong("contactId");
            Date createDate = getJSONLongAsDate(json, "createDate");
            boolean defaultUser = json.getBoolean("defaultUser");
            String emailAddress = json.getString("emailAddress");
            boolean emailAddressVerified = json.getBoolean("emailAddressVerified");
            long facebookId = json.getLong("facebookId");
            int failedLoginAttempts = json.getInt("failedLoginAttempts");
            String firstName = json.getString("firstName");
            int graceLoginCount = json.getInt("graceLoginCount");
            String greeting = json.getString("greeting");
            String jobTitle = json.getString("jobTitle");
            String languageId = json.getString("languageId");
            Date lastFailedLoginDate = getJSONLongAsDate(json, "lastFailedLoginDate");
            Date lastLoginDate = getJSONLongAsDate(json, "lastLoginDate");
            String lastLoginIP = json.getString("lastLoginIP");
            String lastName = json.getString("lastName");
            boolean lockout = json.getBoolean("lockout");
            Date lockoutDate = getJSONLongAsDate(json, "lockoutDate");
            Date loginDate = getJSONLongAsDate(json, "loginDate");
            String loginIP = json.getString("loginIP");
            String middleName = json.getString("middleName");
            Date modifiedDate = getJSONLongAsDate(json, "modifiedDate");
            String openId = json.getString("openId");
            long portraitId = json.getLong("portraitId");
            String reminderQueryAnswer = json.getString("reminderQueryAnswer");
            String reminderQueryQuestion = json.getString("reminderQueryQuestion");
            String screenName = json.getString("screenName");
            int status = json.getInt("status");
            String timeZoneId = json.getString("timeZoneId");
            long userId = json.getLong("userId");
            String uuid = json.getString("uuid");
            return new AutoParcel_User(
                    agreedToTermsOfUse, comments, companyId, contactId, createDate,
                    defaultUser, emailAddress, emailAddressVerified, facebookId,
                    failedLoginAttempts, firstName, graceLoginCount, greeting, jobTitle,
                    languageId, lastFailedLoginDate, lastLoginDate, lastLoginIP, lastName,
                    lockout, lockoutDate, loginDate, loginIP, middleName, modifiedDate,
                    openId, portraitId, reminderQueryAnswer, reminderQueryQuestion, screenName,
                    status, timeZoneId, userId, UUID.fromString(uuid));
        }
    };
}

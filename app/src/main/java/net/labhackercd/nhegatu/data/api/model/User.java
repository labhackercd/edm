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
import java.util.Date;

import static net.labhackercd.nhegatu.data.api.model.util.JSON.getJSONLongAsDate;

public class User implements Serializable {

    private boolean agreedToTermsOfUse;
    private String comments;
    private long companyId;
    private long contactId;
    private Date createDate;
    private boolean defaultUser;
    private String emailAddress;
    private boolean emailAddressVerified;
    private long facebookId;
    private int failedLoginAttempts;
    private String firstName;
    private int graceLoginCount;
    private String greeting;
    private String jobTitle;
    private String languageId;
    private Date lastFailedLoginDate;
    private Date lastLoginDate;
    private String lastLoginIP;
    private String lastName;
    private boolean lockout;
    private Date lockoutDate;
    private Date loginDate;
    private String loginIP;
    private String middleName;
    private Date modifiedDate;
    private String openId;
    private long portraitId;
    private String reminderQueryAnswer;
    private String reminderQueryQuestion;
    private String screenName;
    private int status;
    private String timeZoneId;
    private long userId;
    private String uuid;

    public boolean hasAgreedToTermsOfUse() {
        return agreedToTermsOfUse;
    }

    public String getComments() {
        return comments;
    }

    public long getCompanyId() {
        return companyId;
    }

    public long getContactId() {
        return contactId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public boolean isDefaultUser() {
        return defaultUser;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isEmailAddressVerified() {
        return emailAddressVerified;
    }

    public long getFacebookId() {
        return facebookId;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getGraceLoginCount() {
        return graceLoginCount;
    }

    public String getGreeting() {
        return greeting;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getLanguageId() {
        return languageId;
    }

    public Date getLastFailedLoginDate() {
        return lastFailedLoginDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public String getLastLoginIP() {
        return lastLoginIP;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isLockout() {
        return lockout;
    }

    public Date getLockoutDate() {
        return lockoutDate;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public String getLoginIP() {
        return loginIP;
    }

    public String getMiddleName() {
        return middleName;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public String getOpenId() {
        return openId;
    }

    public long getPortraitId() {
        return portraitId;
    }

    public String getReminderQueryAnswer() {
        return reminderQueryAnswer;
    }

    public String getReminderQueryQuestion() {
        return reminderQueryQuestion;
    }

    public String getScreenName() {
        return screenName;
    }

    public int getStatus() {
        return status;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUUID() {
        return uuid;
    }

    public static final JSONReader<User> JSON_READER = new JSONReader<User>() {
        @Override
        public User fromJSON(JSONObject json) throws JSONException {
            User instance = new User();

            instance.agreedToTermsOfUse = json.getBoolean("agreedToTermsOfUse");
            instance.comments = json.getString("comments");
            instance.companyId = json.getLong("companyId");
            instance.contactId = json.getLong("contactId");
            instance.createDate = getJSONLongAsDate(json, "createDate");
            instance.defaultUser = json.getBoolean("defaultUser");
            instance.emailAddress = json.getString("emailAddress");
            instance.emailAddressVerified = json.getBoolean("emailAddressVerified");
            instance.facebookId = json.getLong("facebookId");
            instance.failedLoginAttempts = json.getInt("failedLoginAttempts");
            instance.firstName = json.getString("firstName");
            instance.graceLoginCount = json.getInt("graceLoginCount");
            instance.greeting = json.getString("greeting");
            instance.jobTitle = json.getString("jobTitle");
            instance.languageId = json.getString("languageId");
            instance.lastFailedLoginDate = getJSONLongAsDate(json, "lastFailedLoginDate");
            instance.lastLoginDate = getJSONLongAsDate(json, "lastLoginDate");
            instance.lastLoginIP = json.getString("lastLoginIP");
            instance.lastName = json.getString("lastName");
            instance.lockout = json.getBoolean("lockout");
            instance.lockoutDate = getJSONLongAsDate(json, "lockoutDate");
            instance.loginDate = getJSONLongAsDate(json, "loginDate");
            instance.loginIP = json.getString("loginIP");
            instance.middleName = json.getString("middleName");
            instance.modifiedDate = getJSONLongAsDate(json, "modifiedDate");
            instance.openId = json.getString("openId");
            instance.portraitId = json.getLong("portraitId");
            instance.reminderQueryAnswer = json.getString("reminderQueryAnswer");
            instance.reminderQueryQuestion = json.getString("reminderQueryQuestion");
            instance.screenName = json.getString("screenName");
            instance.status = json.getInt("status");
            instance.timeZoneId = json.getString("timeZoneId");
            instance.userId = json.getLong("userId");
            instance.uuid = json.getString("uuid");

            return instance;
        }
    };
}

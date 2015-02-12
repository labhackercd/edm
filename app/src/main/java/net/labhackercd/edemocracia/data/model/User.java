package net.labhackercd.edemocracia.data.model;

import java.util.Date;

public class User {
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
}

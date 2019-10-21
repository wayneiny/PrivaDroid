package com.weichengcao.privadroid.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    private static final String APP_PREFERENCES = "PrivaDroidPreferences";

    private Context context;
    private SharedPreferences prefs;

    public UserPreferences(Context context) {
        this.context = context;
    }

    private SharedPreferences getAppPrefs() {
        if (prefs == null) {
            prefs = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        }
        return prefs;
    }

    /**
     * Advertising Id
     */
    private static final String ADVERTISING_ID = "ADVERTISING_ID";

    public String getAdvertisingId() {
        return getAppPrefs().getString(ADVERTISING_ID, "");
    }

    public void setAdvertisingId(String advertisingId) {
        getAppPrefs().edit().putString(ADVERTISING_ID, advertisingId).apply();
    }

    /**
     * If user agrees to the terms, voluntarily joining if user limit reached or not from target country.
     */
    private static final String CONSENT_GRANTED = "CONSENT_GRANTED";

    public boolean getConsentGranted() {
        return getAppPrefs().getBoolean(CONSENT_GRANTED, false);
    }

    public void setConsentGranted(boolean consentGranted) {
        getAppPrefs().edit().putBoolean(CONSENT_GRANTED, consentGranted).apply();
    }

    /**
     * User join date
     */
    private static final String JOIN_DATE = "JOIN_DATE";
    public static final String UNKNOWN_DATE = "UNKNOWN_DATE";

    public String getJoinDate() {
        return getAppPrefs().getString(JOIN_DATE, UNKNOWN_DATE);
    }

    public void setJoinDate(String joinDate) {
        getAppPrefs().edit().putString(JOIN_DATE, joinDate).apply();
    }

    /**
     * Demographic survey.
     */
    private static final String ANSWERED_DEMOGRAPHIC_SURVEY = "ANSWERED_DEMOGRAPHIC_SURVEY";

    public boolean getAnsweredDemographicSurvey() {
        return getAppPrefs().getBoolean(ANSWERED_DEMOGRAPHIC_SURVEY, false);
    }

    public void setAnsweredDemographicSurvey(boolean answeredDemographicSurvey) {
        getAppPrefs().edit().putBoolean(ANSWERED_DEMOGRAPHIC_SURVEY, answeredDemographicSurvey).apply();
    }

    /**
     * Exit survey.
     */
    private static final String ANSWERED_EXIT_SURVEY = "ANSWERED_EXIT_SURVEY";

    public boolean getAnsweredExitSurvey() {
        return getAppPrefs().getBoolean(ANSWERED_EXIT_SURVEY, false);
    }

    public void setAnsweredExitSurvey(boolean answeredExitSurvey) {
        getAppPrefs().edit().putBoolean(ANSWERED_EXIT_SURVEY, answeredExitSurvey).apply();
    }

    /**
     * If user joined despite the limit was reached.
     */
    private static final String USER_LIMIT_REACHED = "USER_LIMIT_REACHED";

    public boolean getUserLimitReached() {
        return getAppPrefs().getBoolean(USER_LIMIT_REACHED, false);
    }

    public void setUserLimitReached(boolean userLimitReached) {
        getAppPrefs().edit().putBoolean(USER_LIMIT_REACHED, userLimitReached).apply();
    }

    /**
     * If user joined despite not in the target countries.
     */
    private static final String USER_NOT_FROM_TARGET_COUNTRY = "USER_NOT_FROM_TARGET_COUNTRY";

    public boolean getUserNotFromTargetCountry() {
        return getAppPrefs().getBoolean(USER_NOT_FROM_TARGET_COUNTRY, false);
    }

    public void setUserNotFromTargetCountry(boolean userNotFromTargetCountry) {
        getAppPrefs().edit().putBoolean(USER_NOT_FROM_TARGET_COUNTRY, userNotFromTargetCountry).apply();
    }

    /**
     * When is the last notification created.
     */
    private static final String LAST_NOTIFICATION_TIMESTAMP = "LAST_NOTIFICATION_TIMESTAMP";

    public String getLastNotificationTimestamp() {
        return getAppPrefs().getString(LAST_NOTIFICATION_TIMESTAMP, "");
    }

    public void setLastNotificationTimestamp(String lastNotificationTimestamp) {
        getAppPrefs().edit().putString(LAST_NOTIFICATION_TIMESTAMP, lastNotificationTimestamp).apply();
    }

    /**
     * When the heartbeat and demographic reminder jobs run
     */
    private static final String LAST_DEMOGRAPHIC_REMINDER = "LAST_DEMOGRAPHIC_REMINDER";
    private static final String LAST_HEARTBEAT_REMINDER = "LAST_HEARTBEAT_REMINDER";

    public String getLastDemographicReminder() {
        return getAppPrefs().getString(LAST_DEMOGRAPHIC_REMINDER, "");
    }

    public void setLastDemographicReminder(String lastDemographicReminder) {
        getAppPrefs().edit().putString(LAST_DEMOGRAPHIC_REMINDER, lastDemographicReminder).apply();
    }

    public String getLastHeartbeatReminder() {
        return getAppPrefs().getString(LAST_HEARTBEAT_REMINDER, "");
    }

    public void setLastHeartbeatReminder(String lastHeartbeatReminder) {
        getAppPrefs().edit().putString(LAST_HEARTBEAT_REMINDER, lastHeartbeatReminder).apply();
    }
}

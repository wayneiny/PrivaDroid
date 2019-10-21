package com.weichengcao.privadroid.database;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.weichengcao.privadroid.BuildConfig;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;
import java.util.Locale;

import static com.weichengcao.privadroid.util.EventUtil.AGE;
import static com.weichengcao.privadroid.util.EventUtil.ANDROID_VERSION;
import static com.weichengcao.privadroid.util.EventUtil.APP_NAME;
import static com.weichengcao.privadroid.util.EventUtil.APP_VERSION;
import static com.weichengcao.privadroid.util.EventUtil.CARRIER;
import static com.weichengcao.privadroid.util.EventUtil.COUNTRY;
import static com.weichengcao.privadroid.util.EventUtil.COUNTRY_CODE;
import static com.weichengcao.privadroid.util.EventUtil.DAILY_USAGE;
import static com.weichengcao.privadroid.util.EventUtil.EDUCATION;
import static com.weichengcao.privadroid.util.EventUtil.EVENT_SERVER_ID;
import static com.weichengcao.privadroid.util.EventUtil.GENDER;
import static com.weichengcao.privadroid.util.EventUtil.GRANTED;
import static com.weichengcao.privadroid.util.EventUtil.INCOME;
import static com.weichengcao.privadroid.util.EventUtil.INDUSTRY;
import static com.weichengcao.privadroid.util.EventUtil.INITIATED_BY_USER;
import static com.weichengcao.privadroid.util.EventUtil.LOCALE;
import static com.weichengcao.privadroid.util.EventUtil.LOGGED_TIME;
import static com.weichengcao.privadroid.util.EventUtil.PACKAGE_NAME;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_REQUESTED_NAME;
import static com.weichengcao.privadroid.util.EventUtil.PHONE_MAKE;
import static com.weichengcao.privadroid.util.EventUtil.PHONE_MODEL;
import static com.weichengcao.privadroid.util.EventUtil.STATUS;
import static com.weichengcao.privadroid.util.EventUtil.SURVEY_ID;
import static com.weichengcao.privadroid.util.EventUtil.USER_AD_ID;

public class ExperimentEventFactory {

    public static HashMap<String, String> createJoinEvent() {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(PHONE_MAKE, Build.MANUFACTURER);
        event.put(PHONE_MODEL, Build.MODEL);
        event.put(ANDROID_VERSION, Build.VERSION.RELEASE);
        event.put(LOCALE, Locale.getDefault().getISO3Language());
        TelephonyManager manager = (TelephonyManager) PrivaDroidApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            event.put(CARRIER, manager.getNetworkOperatorName());
            event.put(COUNTRY_CODE, manager.getNetworkCountryIso());
        }
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        event.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);
        UserPreferences userPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
        event.put(EventUtil.PARTICIPATE_WITH_NO_PAY, Boolean.toString(userPreferences.getUserLimitReached() || userPreferences.getUserNotFromTargetCountry()));

        return event;
    }

    public static HashMap<String, String> createAppInstallEvent(String appName, String packageName, String version) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(APP_NAME, appName);
        event.put(PACKAGE_NAME, packageName);
        event.put(APP_VERSION, version);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        event.put(SURVEY_ID, "");
        event.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return event;
    }

    public static HashMap<String, String> createAppUninstallEvent(String packageName, String appName, String version) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(PACKAGE_NAME, packageName);
        event.put(APP_VERSION, version);
        event.put(APP_NAME, appName);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        event.put(SURVEY_ID, "");
        event.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return event;
    }

    public static HashMap<String, String> createPermissionEvent(String appName, String packageName, String version,
                                                                String permissionName, String granted, String userInitiated,
                                                                String rationaleMessage, String proactivePermissionRequestCorrelationId,
                                                                String previousScreenContext, long packageTotalForegroundTime,
                                                                long packageRecentForegroundTime, long permissionDialogReadTime) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(APP_NAME, appName);
        event.put(APP_VERSION, version);
        event.put(PACKAGE_NAME, packageName);
        event.put(PERMISSION_REQUESTED_NAME, permissionName);
        event.put(GRANTED, granted);
        event.put(INITIATED_BY_USER, userInitiated);
        event.put(EventUtil.PREVIOUS_SCREEN_CONTEXT, previousScreenContext);
        event.put(EventUtil.PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID, proactivePermissionRequestCorrelationId);
        event.put(EventUtil.PROACTIVE_RATIONALE_MESSAGE, rationaleMessage);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        event.put(SURVEY_ID, "");
        event.put(EventUtil.PACKAGE_TOTAL_FOREGROUND_TIME, packageTotalForegroundTime + "");
        event.put(EventUtil.PACKAGE_RECENT_FOREGROUND_TIME, packageRecentForegroundTime + "");
        event.put(EventUtil.PERMISSION_DIALOG_READ_TIME, permissionDialogReadTime + "");
        event.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return event;
    }

    public static HashMap<String, String> createDemographicEvent(String age, String country, String industry,
                                                                 String income, String education, String usage,
                                                                 String status, String gender) {
        HashMap<String, String> event = new HashMap<>();

        event.put(AGE, age);
        event.put(COUNTRY, country);
        event.put(INDUSTRY, industry);
        event.put(INCOME, income);
        event.put(EDUCATION, education);
        event.put(DAILY_USAGE, usage);
        event.put(STATUS, status);
        event.put(GENDER, gender);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return event;
    }

    public static HashMap<String, String> createAppInstallSurveyEvent(String why, String factors,
                                                                      String knowPermission,
                                                                      String thinkPermissions,
                                                                      String eventServerId) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.WHY_INSTALL, why);
        res.put(EventUtil.INSTALL_FACTORS, factors);
        res.put(EventUtil.KNOW_PERMISSION_REQUIRED, knowPermission);
        res.put(EventUtil.PERMISSIONS_THINK_REQUIRED, thinkPermissions);
        res.put(EventUtil.EVENT_SERVER_ID, eventServerId);
        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createAppUninstallSurveyEvent(String why, String permissionsRememberedRequested,
                                                                        String eventServerId) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.WHY_UNINSTALL, why);
        res.put(EventUtil.PERMISSION_REMEMBERED_REQUESTED, permissionsRememberedRequested);
        res.put(EVENT_SERVER_ID, eventServerId);
        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createPermissionGrantSurveyEvent(String whyGrant, String expected,
                                                                           String comfortable,
                                                                           String eventServerId,
                                                                           String temporary,
                                                                           String reminder) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.WHY_GRANT, whyGrant);
        res.put(EventUtil.EXPECTED_PERMISSION_REQUEST, expected);
        res.put(EventUtil.COMFORT_LEVEL, comfortable);
        res.put(EVENT_SERVER_ID, eventServerId);
        res.put(EventUtil.WANT_TEMPORARY_GRANT_ONLY, temporary);
        res.put(EventUtil.WOULD_LIKE_A_NOTIFICATION, reminder);

        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createPermissionDenySurveyEvent(String whyDeny, String expected,
                                                                          String comfortable,
                                                                          String eventServerId) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.WHY_DENY, whyDeny);
        res.put(EventUtil.EXPECTED_PERMISSION_REQUEST, expected);
        res.put(EventUtil.COMFORT_LEVEL, comfortable);
        res.put(EVENT_SERVER_ID, eventServerId);
        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createProactivePermissionEvent(String appName, String packageName,
                                                                         String appVersion, String rationale,
                                                                         String granted, String permissionEventCorrelationId) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.PROACTIVE_RATIONALE_MESSAGE, rationale);
        res.put(EventUtil.PROACTIVE_REQUEST_GRANTED, granted);
        res.put(EventUtil.PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID, permissionEventCorrelationId);
        res.put(APP_NAME, appName);
        res.put(APP_VERSION, appVersion);
        res.put(PACKAGE_NAME, packageName);
        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createRewardsMethodEvent(String rewardsMethod, String methodValue) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.REWARDS_METHOD, rewardsMethod);
        res.put(EventUtil.REWARDS_METHOD_VALUE, methodValue);
        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.REWARDS_JOIN_DATE, new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createExitSurveyEvent(String controlOne, String controlTwo,
                                                                String controlThree, String awarenessOne,
                                                                String awarenessTwo, String awarenessThree,
                                                                String collectionOne, String collectionTwo,
                                                                String collectionThree, String errorOne,
                                                                String errorTwo, String errorThree,
                                                                String errorFour, String secondaryUseOne,
                                                                String secondaryUseTwo, String secondaryUseThree,
                                                                String secondaryUseFour, String secondaryUseFive,
                                                                String improperOne, String improperTwo,
                                                                String improperThree, String globalOne,
                                                                String globalTwo, String globalThree, String globalFour,
                                                                String globalFive, String familiar, String dontUnderstand) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.CONTROL_ONE, controlOne);
        res.put(EventUtil.CONTROL_TWO, controlTwo);
        res.put(EventUtil.CONTROL_THREE, controlThree);
        res.put(EventUtil.AWARENESS_ONE, awarenessOne);
        res.put(EventUtil.AWARENESS_TWO, awarenessTwo);
        res.put(EventUtil.AWARENESS_THREE, awarenessThree);
        res.put(EventUtil.COLLECTION_ONE, collectionOne);
        res.put(EventUtil.COLLECTION_TWO, collectionTwo);
        res.put(EventUtil.COLLECTION_THREE, collectionThree);
        res.put(EventUtil.ERROR_ONE, errorOne);
        res.put(EventUtil.ERROR_TWO, errorTwo);
        res.put(EventUtil.ERROR_THREE, errorThree);
        res.put(EventUtil.ERROR_FOUR, errorFour);
        res.put(EventUtil.SECONDARY_USE_ONE, secondaryUseOne);
        res.put(EventUtil.SECONDARY_USE_TWO, secondaryUseTwo);
        res.put(EventUtil.SECONDARY_USE_THREE, secondaryUseThree);
        res.put(EventUtil.SECONDARY_USE_FOUR, secondaryUseFour);
        res.put(EventUtil.SECONDARY_USE_FIVE, secondaryUseFive);
        res.put(EventUtil.IMPROPER_ONE, improperOne);
        res.put(EventUtil.IMPROPER_TWO, improperTwo);
        res.put(EventUtil.IMPROPER_THREE, improperThree);
        res.put(EventUtil.GLOBAL_ONE, globalOne);
        res.put(EventUtil.GLOBAL_TWO, globalTwo);
        res.put(EventUtil.GLOBAL_THREE, globalThree);
        res.put(EventUtil.GLOBAL_FOUR, globalFour);
        res.put(EventUtil.GLOBAL_FIVE, globalFive);
        res.put(EventUtil.FAMILIAR_WITH_ANDROID_PERMISSION, familiar);
        res.put(EventUtil.PERMISSIONS_THAT_DONT_UNDERSTAND, dontUnderstand);

        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createHeartbeatEvent(String accessibilityServiceOn, String appUsageAccessOn) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.ACCESSIBILITY_ACCESS_ON, accessibilityServiceOn);
        res.put(EventUtil.APP_USAGE_ACCESS_ON, appUsageAccessOn);

        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createRevokePermissionReminderNotificationClickEvent(String clicked, String permissionGrantSurveyServerDocId) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.REVOKE_NOTIFICATION_CLICKED, clicked);
        res.put(EventUtil.PERMISSION_GRANT_SURVEY_SERVER_DOC_ID, permissionGrantSurveyServerDocId);

        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    public static HashMap<String, String> createDemographicReminderLogEvent() {
        HashMap<String, String> res = new HashMap<>();

        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }

    static HashMap<String, String> createLocalStorageSyncEvent(String numberOfEventsSynced) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.NUMBER_OF_EVENTS_SYNCED, numberOfEventsSynced);

        res.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        res.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        res.put(EventUtil.PRIVADROID_VERSION, BuildConfig.VERSION_NAME);

        return res;
    }
}

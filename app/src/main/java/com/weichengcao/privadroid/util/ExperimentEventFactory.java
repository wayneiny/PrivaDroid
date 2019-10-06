package com.weichengcao.privadroid.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.weichengcao.privadroid.BuildConfig;
import com.weichengcao.privadroid.PrivaDroidApplication;

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
                                                                String rationaleMessage, String proactivePermissionRequestCorrelationId) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(APP_NAME, appName);
        event.put(APP_VERSION, version);
        event.put(PACKAGE_NAME, packageName);
        event.put(PERMISSION_REQUESTED_NAME, permissionName);
        event.put(GRANTED, granted);
        event.put(INITIATED_BY_USER, userInitiated);
        event.put(EventUtil.PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID, proactivePermissionRequestCorrelationId);
        event.put(EventUtil.PROACTIVE_RATIONALE_MESSAGE, rationaleMessage);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());
        event.put(SURVEY_ID, "");
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
                                                                           String eventServerId) {
        HashMap<String, String> res = new HashMap<>();

        res.put(EventUtil.WHY_GRANT, whyGrant);
        res.put(EventUtil.EXPECTED_PERMISSION_REQUEST, expected);
        res.put(EventUtil.COMFORT_LEVEL, comfortable);
        res.put(EVENT_SERVER_ID, eventServerId);
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
}

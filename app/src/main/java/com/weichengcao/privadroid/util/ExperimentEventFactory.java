package com.weichengcao.privadroid.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.weichengcao.privadroid.PrivaDroidApplication;

import java.util.HashMap;

import static com.weichengcao.privadroid.util.EventUtil.AGE;
import static com.weichengcao.privadroid.util.EventUtil.ANDROID_VERSION;
import static com.weichengcao.privadroid.util.EventUtil.APP_NAME;
import static com.weichengcao.privadroid.util.EventUtil.APP_VERSION;
import static com.weichengcao.privadroid.util.EventUtil.CARRIER;
import static com.weichengcao.privadroid.util.EventUtil.COUNTRY;
import static com.weichengcao.privadroid.util.EventUtil.DAILY_USAGE;
import static com.weichengcao.privadroid.util.EventUtil.EDUCATION;
import static com.weichengcao.privadroid.util.EventUtil.GENDER;
import static com.weichengcao.privadroid.util.EventUtil.GRANTED;
import static com.weichengcao.privadroid.util.EventUtil.INCOME;
import static com.weichengcao.privadroid.util.EventUtil.INDUSTRY;
import static com.weichengcao.privadroid.util.EventUtil.INITIATED_BY_USER;
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
        TelephonyManager manager = (TelephonyManager) PrivaDroidApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null && manager.getNetworkOperatorName() != null) {
            String carrierName = manager.getNetworkOperatorName();
            event.put(CARRIER, carrierName);
        }
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());

        return event;
    }

    public static HashMap<String, String> createAppInstallEvent(String appName, String packageName, String version) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(APP_NAME, appName);
        event.put(PACKAGE_NAME, packageName);
        event.put(APP_VERSION, version);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());

        // TODO: currently don't survey the users
        event.put(SURVEY_ID, Boolean.toString(false));

        return event;
    }

    public static HashMap<String, String> createAppUninstallEvent(String packageName, String appName, String version) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(PACKAGE_NAME, packageName);
        event.put(APP_VERSION, version);
        event.put(APP_NAME, appName);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());

        // TODO: currently don't survey the users
        event.put(SURVEY_ID, Boolean.toString(false));

        return event;
    }

    public static HashMap<String, String> createPermissionEvent(String appName, String packageName, String version,
                                                                String permissionName, String granted, String userInitiated) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(APP_NAME, appName);
        event.put(APP_VERSION, version);
        event.put(PACKAGE_NAME, packageName);
        event.put(PERMISSION_REQUESTED_NAME, permissionName);
        event.put(GRANTED, granted);
        event.put(INITIATED_BY_USER, userInitiated);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());

        // TODO: currently don't survey the users
        event.put(SURVEY_ID, Boolean.toString(false));

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

        return event;
    }
}

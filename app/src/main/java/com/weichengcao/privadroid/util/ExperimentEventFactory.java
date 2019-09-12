package com.weichengcao.privadroid.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.weichengcao.privadroid.PrivaDroidApplication;

import java.util.HashMap;

import static com.weichengcao.privadroid.util.EventConstants.ANDROID_VERSION;
import static com.weichengcao.privadroid.util.EventConstants.APP_NAME;
import static com.weichengcao.privadroid.util.EventConstants.APP_VERSION;
import static com.weichengcao.privadroid.util.EventConstants.CARRIER;
import static com.weichengcao.privadroid.util.EventConstants.GRANTED;
import static com.weichengcao.privadroid.util.EventConstants.INITIATED_BY_USER;
import static com.weichengcao.privadroid.util.EventConstants.LOGGED_TIME;
import static com.weichengcao.privadroid.util.EventConstants.PACKAGE_NAME;
import static com.weichengcao.privadroid.util.EventConstants.PERMISSION_REQUESTED_NAME;
import static com.weichengcao.privadroid.util.EventConstants.PHONE_MAKE;
import static com.weichengcao.privadroid.util.EventConstants.PHONE_MODEL;
import static com.weichengcao.privadroid.util.EventConstants.SURVEYED;
import static com.weichengcao.privadroid.util.EventConstants.USER_AD_ID;

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
        event.put(SURVEYED, Boolean.toString(false));

        return event;
    }

    public static HashMap<String, String> createAppUninstallEvent(String packageName) {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, new UserPreferences(PrivaDroidApplication.getAppContext()).getAdvertisingId());
        event.put(PACKAGE_NAME, packageName);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());

        // TODO: currently don't survey the users
        event.put(SURVEYED, Boolean.toString(false));

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
        event.put(SURVEYED, Boolean.toString(false));

        return event;
    }
}

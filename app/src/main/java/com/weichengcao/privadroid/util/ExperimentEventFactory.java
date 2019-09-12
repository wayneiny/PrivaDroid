package com.weichengcao.privadroid.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.weichengcao.privadroid.PrivaDroidApplication;

import java.util.HashMap;

import static com.weichengcao.privadroid.util.FirestoreConstants.ANDROID_VERSION;
import static com.weichengcao.privadroid.util.FirestoreConstants.APP_NAME;
import static com.weichengcao.privadroid.util.FirestoreConstants.APP_VERSION;
import static com.weichengcao.privadroid.util.FirestoreConstants.CARRIER;
import static com.weichengcao.privadroid.util.FirestoreConstants.LOGGED_TIME;
import static com.weichengcao.privadroid.util.FirestoreConstants.PACKAGE_NAME;
import static com.weichengcao.privadroid.util.FirestoreConstants.PHONE_MAKE;
import static com.weichengcao.privadroid.util.FirestoreConstants.PHONE_MODEL;
import static com.weichengcao.privadroid.util.FirestoreConstants.USER_AD_ID;

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

        event.put(APP_NAME, appName);
        event.put(PACKAGE_NAME, packageName);
        event.put(APP_VERSION, version);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());

        return event;
    }

    public static HashMap<String, String> createAppUninstallEvent(String packageName) {
        HashMap<String, String> event = new HashMap<>();

        event.put(PACKAGE_NAME, packageName);
        event.put(LOGGED_TIME, DatetimeUtil.getCurrentIsoDatetime());

        return event;
    }
}

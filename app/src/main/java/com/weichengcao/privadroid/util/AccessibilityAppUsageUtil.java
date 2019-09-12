package com.weichengcao.privadroid.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.sensors.AccessibilityEventMonitorService;

public class AccessibilityAppUsageUtil {

    private static final String TAG = AccessibilityAppUsageUtil.class.getSimpleName();

    public static boolean readHowTo = false;

    public static boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        final String service = PrivaDroidApplication.getAppContext().getPackageName() + "/" + AccessibilityEventMonitorService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    PrivaDroidApplication.getAppContext().getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    PrivaDroidApplication.getAppContext().getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isAppUsageSettingsOn() {
        try {
            PackageManager packageManager = PrivaDroidApplication.getAppContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(PrivaDroidApplication.getAppContext().getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) PrivaDroidApplication.getAppContext().getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}

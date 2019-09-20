package com.weichengcao.privadroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationNameFromPackageName;

public class ApplicationInfoPreferences {

    private static final String TAG = ApplicationInfoPreferences.class.getSimpleName();

    private static final String APPLICATION_PREFERENCES = "ApplicationInfoPreferences";

    private Context context;
    private SharedPreferences prefs;

    public ApplicationInfoPreferences(Context context) {
        this.context = context;
    }

    private SharedPreferences getAppPrefs() {
        if (prefs == null) {
            prefs = context.getSharedPreferences(APPLICATION_PREFERENCES, Context.MODE_PRIVATE);
        }
        return prefs;
    }

    /**
     * ${packageName}_app_name : appName
     * ${packageName}_version : appVersion
     */
    private String createPackageToNameKey(String packageName) {
        return packageName + "_app_name";
    }

    private String createPackageToVersionKey(String packageName) {
        return packageName + "_version";
    }

    /**
     * Store package name to app name mappings.
     */
    public String getApplicationName(String packageName) {
        return getAppPrefs().getString(createPackageToNameKey(packageName), "");
    }

    public void setApplicationName(String packageName, String appName) {
        getAppPrefs().edit().putString(createPackageToNameKey(packageName), appName).apply();
    }

    /**
     * Store package name to app version mappings.
     */
    public String getApplicationVersion(String packageName) {
        return getAppPrefs().getString(createPackageToVersionKey(packageName), "");
    }

    public void setApplicationVersion(String packageName, String appVersion) {
        getAppPrefs().edit().putString(createPackageToVersionKey(packageName), appVersion).apply();
    }

    /**
     * Store flag to indicate if we cached application info for all the apps.
     */
    private static final String CACHED_APP_INFO = "CACHED_APP_INFO";

    public boolean getCachedAppInfo() {
        return getAppPrefs().getBoolean(CACHED_APP_INFO, false);
    }

    public void setCachedAppInfo(boolean cachedAppInfo) {
        getAppPrefs().edit().putBoolean(CACHED_APP_INFO, cachedAppInfo).apply();
    }

    /**
     * Method that performs caching of the application info.
     */
    public void cacheAppInfo() {
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo ai : packages) {
            /**
             * Cache app name.
             */
            String packageName = ai.packageName;
            String appName = getApplicationNameFromPackageName(packageName, context.getPackageManager());
            setApplicationName(packageName, appName);

            /**
             * Cache app version.
             */
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
                setApplicationVersion(packageName, pi.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot find packageInfo of " + packageName);
            }
        }
    }
}

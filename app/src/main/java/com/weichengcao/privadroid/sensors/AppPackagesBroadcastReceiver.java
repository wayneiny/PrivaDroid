package com.weichengcao.privadroid.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.ApplicationInfoPreferences;
import com.weichengcao.privadroid.util.ExperimentEventFactory;

import java.util.HashMap;
import java.util.List;

public class AppPackagesBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = AppPackagesBroadcastReceiver.class.getSimpleName();

    private PackageManager packageManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isPackageRemoved(context, intent)) {
//            Log.d(TAG, "Detected package removed.");
            logPackageUninstallEvent(context, intent);
        } else if (isPackageAdded(context, intent)) {
//            Log.d(TAG, "Detected package installed.");
            logPackageInstallEvent(context, intent);
        }
    }

    /**
     * Extract package name from system broadcast intent.
     */
    private String getPackageNameFromIntent(Intent intent) {
        Uri data = intent.getData();
        if (data == null || data.getEncodedSchemeSpecificPart() == null) {
            return null;
        }
        return data.getEncodedSchemeSpecificPart();
    }

    /**
     * Get application name from package name using package manager.
     */
    public static String getApplicationNameFromPackageName(String packageName, PackageManager packageManager) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return applicationInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Find application version by package name.
     */
    public static String getApplicationVersion(String packageName, PackageManager packageManager) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Find the package name using app name.
     */
    public static String findPackageNameFromAppName(String appName, PackageManager packageManager) {
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo ai : packages) {
            String packageName = ai.packageName;
            String packageAppName = getApplicationNameFromPackageName(packageName, packageManager);
            if (appName.equalsIgnoreCase(packageAppName)) {
                return packageName;
            }
        }

        return null;
    }

    // log events and send to Firestore [START]
    private void logPackageInstallEvent(Context context, Intent intent) {
        packageManager = context.getPackageManager();

        // 1. extract required app information
        String packageName = getPackageNameFromIntent(intent);
        if (packageName == null) {
            return;
        }
        String appName = getApplicationNameFromPackageName(packageName, packageManager);
        String version = getApplicationVersion(packageName, packageManager);

        /**
         * 1.1 Store new app info to ApplicationInfoPreferences.
         */
        ApplicationInfoPreferences applicationInfoPreferences = new ApplicationInfoPreferences(context);
        applicationInfoPreferences.setApplicationVersion(packageName, version);
        applicationInfoPreferences.setApplicationName(packageName, appName);

        // 2. send event to Firestore
        FirestoreProvider fp = new FirestoreProvider();
        HashMap<String, String> event = ExperimentEventFactory.createAppInstallEvent(appName, packageName, version);
        fp.sendAppInstallEvent(event, true);
    }

    private void logPackageUninstallEvent(Context context, Intent intent) {
        packageManager = context.getPackageManager();

        // 1. log event and send to Firestore
        String packageName = getPackageNameFromIntent(intent);
        if (packageName == null) {
            return;
        }

        ApplicationInfoPreferences applicationInfoPreferences = new ApplicationInfoPreferences(context);
        String appName = applicationInfoPreferences.getApplicationName(packageName);
        String appVersion = applicationInfoPreferences.getApplicationVersion(packageName);

        // 2. send event to Firestore
        FirestoreProvider fp = new FirestoreProvider();
        HashMap<String, String> event = ExperimentEventFactory.createAppUninstallEvent(packageName, appName, appVersion);
        fp.sendAppUninstallEvent(event, true);
    }
    // log events and send to Firestore [END]


    // detect what kind of broadcasts [START]
    private boolean isPackageRemoved(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }

        return (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) || intent.getAction().equals(Intent.ACTION_PACKAGE_FULLY_REMOVED))
                && !isPackageUpdate(intent);
    }

    private boolean isPackageUpdate(Intent intent) {
        // If EXTRA_REPLACING is not present (or if it is present but false), return false.
        return intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
    }

    private boolean isPackageAdded(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return false;
        }

        return (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) && !isPackageUpdate(intent));
    }
    // detect what kind of broadcasts [END]
}

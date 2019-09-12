package com.weichengcao.privadroid.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.ExperimentEventFactory;

import java.util.HashMap;

public class SystemBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SystemBroadcastReceiver.class.getSimpleName();

    private PackageManager packageManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isPackageRemoved(context, intent)) {
            logPackageUninstallEvent(context, intent);
        } else if (isPackageAdded(context, intent)) {
            logPackageInstallEvent(context, intent);
        }
    }


    // util [START]
    private String getPackageNameFromIntent(Intent intent) {
        Uri data = intent.getData();
        if (data == null || data.getEncodedSchemeSpecificPart() == null) {
            return null;
        }
        return data.getEncodedSchemeSpecificPart();
    }

    private String getApplicationNameFromPackageName(String packageName) {
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return applicationInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String getApplicationVersion(String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    // util [END]


    // log events and send to Firestore [START]
    private void logPackageInstallEvent(Context context, Intent intent) {
        packageManager = context.getPackageManager();

        // 1. extract required app information
        String packageName = getPackageNameFromIntent(intent);
        if (packageName == null) {
            return;
        }
        String appName = getApplicationNameFromPackageName(packageName);
        String version = getApplicationVersion(packageName);

        // 2. send event to Firestore
        FirestoreProvider fp = new FirestoreProvider();
        HashMap<String, String> event = ExperimentEventFactory.createAppInstallEvent(appName, packageName, version);
        fp.sendAppInstallEvent(event);
    }

    private void logPackageUninstallEvent(Context context, Intent intent) {
        packageManager = context.getPackageManager();

        // 1. log event and send to Firestore
        String packageName = getPackageNameFromIntent(intent);
        if (packageName == null) {
            return;
        }

        // 2. send event to Firestore
        FirestoreProvider fp = new FirestoreProvider();
        HashMap<String, String> event = ExperimentEventFactory.createAppUninstallEvent(packageName);
        fp.sendAppUninstallEvent(event);
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

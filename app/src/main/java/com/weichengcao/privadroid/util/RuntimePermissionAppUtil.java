package com.weichengcao.privadroid.util;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import com.weichengcao.privadroid.PrivaDroidApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.weichengcao.privadroid.util.AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.M_LAUNCHER_PACKAGE;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.SETTINGS_PACKAGE;

public class RuntimePermissionAppUtil {

    private static final long LOOK_BACK_PERIOD = 60000;

    private static final HashSet<String> EXCLUDED_ACTIVE_APPS = new HashSet<>(Arrays.asList(
            GOOGLE_PACKAGE_INSTALLER_PACKAGE,
            PACKAGE_INSTALLER_PACKAGE,
            M_LAUNCHER_PACKAGE,
            SETTINGS_PACKAGE
    ));

    public static String getLastActiveAppPackageName() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) PrivaDroidApplication.getAppContext().getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        // We get usage stats for the last 5 seconds
        List<UsageStats> stats = null;
        if (usageStatsManager != null) {
            stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - LOOK_BACK_PERIOD, now);
        }
        // Get the next-to-last app from this list.
        String lastUsedApp = null;
        long lastUsedTime = 0;
        if (stats != null) {
            for (UsageStats appStats : stats) {
                if (appStats.getLastTimeUsed() > lastUsedTime && !EXCLUDED_ACTIVE_APPS.contains(appStats.getPackageName())) {
                    lastUsedTime = appStats.getLastTimeUsed();
                    lastUsedApp = appStats.getPackageName();
                }
            }
        }
        return lastUsedApp;
    }
}

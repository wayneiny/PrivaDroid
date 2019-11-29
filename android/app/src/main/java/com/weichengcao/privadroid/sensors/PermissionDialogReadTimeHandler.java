package com.weichengcao.privadroid.sensors;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import com.weichengcao.privadroid.PrivaDroidApplication;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

class PermissionDialogReadTimeHandler {

    static long NANOSECOND_TO_SECOND = 1000000;

    static long permissionDialogFirstOpenTime = 0;
    static long permissionDialogReadTimeInSeconds = 0;

    static long getTotalForegroundTime(String appPackage) {
        if (appPackage == null || appPackage.isEmpty()) {
            return -1;
        }

        // create a time one month from today
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        long start = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();

        // creating usage stats aggregated stats for all apps on phone
        UsageStatsManager usageStatsManager = (UsageStatsManager) PrivaDroidApplication.getAppContext().getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> stats = null;
        if (usageStatsManager != null) {
            stats = usageStatsManager.queryAndAggregateUsageStats(start, now);
        }

        long time = -1;

        //log the total time an app has been in the foreground as an indicating of how recently an user opened the phone
        if (stats != null && stats.get(appPackage) != null) {
            time = stats.get(appPackage).getTotalTimeInForeground();
        }

        return time;
    }

    static long getRecentForegroundTime(String appPackage) {
        if (appPackage == null) {
            return -1;
        }

        // create a time one day from today
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        long start = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();

        // creating usage stats aggregated stats for all apps on phone
        UsageStatsManager usageStatsManager = (UsageStatsManager) PrivaDroidApplication.getAppContext().getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> stats = null;
        if (usageStatsManager != null) {
            stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, now);
        }

        long time = -1;

        if (stats != null) {
            for (UsageStats stat : stats) {
                if (stat.getPackageName().equalsIgnoreCase(appPackage)) {
                    time = stat.getTotalTimeInForeground();
                    break;
                }
            }
        }

        return time;
    }
}

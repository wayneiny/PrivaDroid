package com.weichengcao.privadroid.sensors;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.weichengcao.privadroid.PrivaDroidApplication;

import java.util.List;

import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.FOREGROUND_SERVICE_NOTIFICATION_ID;
import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.createAndroidNotificationForForegroundService;
import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.createNotificationChannel;

public class SystemBroadcastForegroundService extends Service {

    private static final String TAG = SystemBroadcastForegroundService.class.getSimpleName();

    private AppPackagesBroadcastReceiver appPackagesBroadcastReceiver;

    public final static String SYSTEM_BROADCAST_FOREGROUND_SERVICE = "com.weichengcao.privadroid.sensors.SystemBroadcastForegroundService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting foreground service.");

        registerAppPackageReceiver();
        createNotificationChannel();
        Notification notification = createAndroidNotificationForForegroundService();
        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    private void registerAppPackageReceiver() {
        appPackagesBroadcastReceiver = new AppPackagesBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        this.registerReceiver(appPackagesBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        if (appPackagesBroadcastReceiver != null) {
            unregisterReceiver(appPackagesBroadcastReceiver);
            Log.d(TAG, "Foreground service is destroyed.");
        }
        super.onDestroy();
    }

    /**
     * Start SystemBroadcastForegroundService if >= Oreo.
     */
    public static void startSystemBroadcastForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isServiceRunning(SYSTEM_BROADCAST_FOREGROUND_SERVICE, PrivaDroidApplication.getAppContext())) {
                PrivaDroidApplication.getAppContext().startForegroundService(new Intent(PrivaDroidApplication.getAppContext(), SystemBroadcastForegroundService.class));
            }
        }
    }

    /**
     * Check if a service is already running.
     */
    public static boolean isServiceRunning(String serviceName, Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }

        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : l) {
            if (runningServiceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }

        return false;
    }
}

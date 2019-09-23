package com.weichengcao.privadroid.sensors;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.FOREGROUND_SERVICE_NOTIFICATION_ID;
import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.createAndroidNotificationForForegroundService;
import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.createNotificationChannelForForegroundService;

public class SystemBroadcastForegroundService extends Service {

    private static final String TAG = SystemBroadcastForegroundService.class.getSimpleName();

    private AppPackagesBroadcastReceiver appPackagesBroadcastReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting foreground service.");

        registerAppPackageReceiver();
        createNotificationChannelForForegroundService();
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
        unregisterReceiver(appPackagesBroadcastReceiver);
        Log.d(TAG, "Foreground service is destroyed.");
        super.onDestroy();
    }
}

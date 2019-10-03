package com.weichengcao.privadroid.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

import static com.weichengcao.privadroid.sensors.SystemBroadcastForegroundService.startSystemBroadcastForegroundService;

public class SystemChangeEventReceiver extends BroadcastReceiver {

    private final static String TAG = SystemChangeEventReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (intent != null && intent.getAction() != null) {
                Log.d(TAG, "Received system change event broadcast for " + intent.getAction());

                if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                        intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                        intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED) ||
                        (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) && intent.getDataString() != null &&
                                intent.getDataString().startsWith("package" + PrivaDroidApplication.getAppContext().getString(R.string.privadroid_package_name)))) {
                    Log.d(TAG, "Received ACTION_TIMEZONE_CHANGED, ACTION_LOCALE_CHANGED, ACTION_BOOT_COMPLETED or ACTION_PACKAGE_REPLACED for PrivaDroid.");

                    /**
                     * Create notification for SystemBroadcastForegroundService to listen to package install, uninstall.
                     */
                    startSystemBroadcastForegroundService();
                }
            }
        }
    }
}

package com.weichengcao.privadroid.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SystemBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isPackageRemoved(context, intent)) {
            Log.d(TAG, "Package removed broadcast received.");
        } else if (isPackageAdded(context, intent)) {
            Log.d(TAG, "Package installed broadcast received.");
        }
    }

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

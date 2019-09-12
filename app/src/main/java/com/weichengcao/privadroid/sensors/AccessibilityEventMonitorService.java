package com.weichengcao.privadroid.sensors;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AccessibilityEventMonitorService extends AccessibilityService {

    private static final String TAG = AccessibilityEventMonitorService.class.getSimpleName();

    private static boolean running = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence packageName = event.getPackageName();
        int actionType = event.getAction();
        AccessibilityNodeInfo source = event.getSource();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {

        } else {
            Log.d(TAG, "Invalid build version = " + Build.VERSION.SDK_INT);
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    protected void onServiceConnected() {
        running = true;
        Log.i(TAG, "Connected to the accessibility service");
    }

    @Override
    public void onInterrupt() {

    }
}

package com.weichengcao.privadroid.sensors;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccessibilityEventMonitorService extends AccessibilityService {

    private static final String TAG = AccessibilityEventMonitorService.class.getSimpleName();

    public static final List<String> PERMISSION_SETTINGS_STRINGS = Arrays.asList(
            "Camera",
            "Contacts",
            "Location",
            "Microphone",
            "Phone",
            "Storage",
            "Body Sensors",
            "Calendar",
            "SMS",
            "Call Logs"
    );

    public static final Map<String, String> PERMISSION_DIALOG_STRINGS = new HashMap<>();

    static {
        PERMISSION_DIALOG_STRINGS.put("take pictures and record video", "Camera");
        PERMISSION_DIALOG_STRINGS.put("access your contacts", "Contacts");
        PERMISSION_DIALOG_STRINGS.put("access this device's location", "Location");
        PERMISSION_DIALOG_STRINGS.put("record audio", "Microphone");
        PERMISSION_DIALOG_STRINGS.put("make and manage phone calls", "Phone");
        PERMISSION_DIALOG_STRINGS.put("access photos, media, and files on your device", "Storage");
        PERMISSION_DIALOG_STRINGS.put("access photos, media and files on your device", "Storage");  // oxford comma I guess
        PERMISSION_DIALOG_STRINGS.put("access sensor data about your vital signs", "Body Sensors");
        PERMISSION_DIALOG_STRINGS.put("access your calendar", "Calendar");
        PERMISSION_DIALOG_STRINGS.put("send and view SMS messages", "SMS");
        PERMISSION_DIALOG_STRINGS.put("access your call logs", "Call Logs");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!Locale.getDefault().getISO3Language().equals(Locale.ENGLISH.getISO3Language())) {
            // We don't really need to signal this to the user, as it is the experiment provider who
            // is responsible for checking this should not be a problem for the experiment.
            Log.d(TAG, "Detected locale is " + Locale.getDefault().toString() +
                    ". RuntimePermissions triggering does not support non-English languages; " +
                    "permissions might not always be interpreted correctly");
            return;
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            MarshmallowAccessibilityHandler.processAccessibilityEvent(event);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            NougatAccessibilityHandler.processAccessibilityEvent(event);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            NougatMR1AccessibilityHandler.processAccessibilityEvent(event);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            Log.i(TAG, "Accessibility Event in O:26 version");
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            Log.i(TAG, "Accessibility Event in O_MR1:27 version");
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            Log.i(TAG, "Accessibility Event in P:28 version");
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            Log.i(TAG, "Accessibility Event in Q:29 version");
        } else {
            Log.d(TAG, "Invalid build version = " + Build.VERSION.SDK_INT);
        }
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "Connected to the accessibility service.");
    }

    @Override
    public void onInterrupt() {

    }
}

package com.weichengcao.privadroid.sensors;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.google.android.gms.common.util.ArrayUtils;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccessibilityEventMonitorService extends AccessibilityService {

    private static final String TAG = AccessibilityEventMonitorService.class.getSimpleName();

    public static final List<String> PERMISSION_SETTINGS_STRINGS = Arrays.asList(
            PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.android_permission_categories)
    );

    public static final Map<String, String> PERMISSION_DIALOG_STRINGS = new HashMap<>();

    static {
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_camera), "Camera");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_contacts), "Contacts");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_location), "Location");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_microphone), "Microphone");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_phone), "Phone");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_storage_one), "Storage");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_storage_two), "Storage");  // oxford comma I guess
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_body_sensors), "Body Sensors");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_calendar), "Calendar");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_sms), "SMS");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_call_logs_one), "Call Logs");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_call_logs_two), "Call Logs");
    }

    public static final HashSet<String> PERMISSION_RELATED_KEYWORDS = new HashSet<>(
            Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.permission_related_keywords))
    );

    public static final HashSet<String> PERMISSION_RATIONALE_BUTTON_KEYWORDS = new HashSet<>(
            Arrays.asList(
                    ArrayUtils.concat(
                            PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_grant_button_texts),
                            PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_deny_button_texts)
                    ))
    );

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
            OreoAccessibilityHandler.processAccessibilityEvent(event);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            OreoMR1AccessibilityHandler.processAccessibilityEvent(event);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            PieAccessibilityHandler.processAccessibilityEvent(event);
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

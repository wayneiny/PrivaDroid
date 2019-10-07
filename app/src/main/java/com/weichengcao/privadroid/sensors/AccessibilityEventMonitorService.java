package com.weichengcao.privadroid.sensors;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.gms.common.util.ArrayUtils;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

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
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_storage_two), "Storage");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_body_sensors), "Body Sensors");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_calendar), "Calendar");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_sms), "SMS");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_call_logs_one), "Call Logs");
        PERMISSION_DIALOG_STRINGS.put(PrivaDroidApplication.getAppContext().getString(R.string.permission_dialog_string_call_logs_two), "Call Logs");
    }

    public static final HashSet<String> PERMISSION_RELATED_KEYWORDS = new HashSet<>(
            Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.permission_related_keywords))
    );

    public static final HashSet<String> PERMISSION_ACTION_RELATED_KEYWORDS = new HashSet<>(
            Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.permission_action_related_keywords))
    );

    public static final HashSet<String> PERMISSION_RATIONALE_BUTTON_KEYWORDS = new HashSet<>(
            Arrays.asList(
                    ArrayUtils.concat(
                            PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_grant_button_texts),
                            PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_deny_button_texts)
                    ))
    );

    public static final int PROACTIVE_PERMISSION_REQUEST_DIALOG_VIEW_THRESHOLD = 4;

    /**
     * Process up to 5 strings from previous screens.
     */
    public static final int PREVIOUS_SCREENS_SIZE = 5;
    public static String[] previousScreenTexts = new String[PREVIOUS_SCREENS_SIZE];

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!Locale.getDefault().getISO3Language().equals(Locale.ENGLISH.getISO3Language())) {
            return;
        }

        // Don't process if user has not joined the experiment
        if (new UserPreferences(PrivaDroidApplication.getAppContext()).getFirestoreJoinEventId().isEmpty()) {
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

        } else {

        }

        // Record current screen text
        if (event.getSource() != null) {
            StringBuilder sb = new StringBuilder();
            recordPreviousScreenText(event.getSource(), sb);
            System.arraycopy(previousScreenTexts, 0, previousScreenTexts, 1, PREVIOUS_SCREENS_SIZE - 1);
            previousScreenTexts[0] = sb.toString();
        }
    }

    /**
     * Record the previous screen text for the use of "Context" detecting.
     */
    private void recordPreviousScreenText(AccessibilityNodeInfo source, StringBuilder sb) {
        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        int level = 1;
        int maxLevel = 3;
        while (!allChildren.isEmpty()) {
            if (level >= maxLevel) {
                return;
            }

            int size = allChildren.size();
            for (int i = 0; i < size; i++) {
                AccessibilityNodeInfo cur = allChildren.poll();

                if (cur != null) {
                    int childCount = cur.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        allChildren.add(cur.getChild(j));
                    }

                    checkNodeSourceText(cur, sb);
                    cur.recycle();
                }
            }

            level++;
        }
    }

    private void checkNodeSourceText(AccessibilityNodeInfo source, StringBuilder sb) {
        if (source.getText() == null || source.getClassName() == null) {
            return;
        }

        String androidWidgetClassName = source.getClassName().toString();
        if (androidWidgetClassName.contains("android.")) {
            androidWidgetClassName = androidWidgetClassName.substring(androidWidgetClassName.lastIndexOf(".") + 1);
        }

        sb.append(androidWidgetClassName).append(": ").append(source.getText()).append("\n");
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "Connected to the accessibility service.");
    }

    @Override
    public void onInterrupt() {

    }
}

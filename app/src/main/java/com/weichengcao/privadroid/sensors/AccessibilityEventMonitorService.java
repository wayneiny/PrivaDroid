package com.weichengcao.privadroid.sensors;

import android.accessibilityservice.AccessibilityService;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.ExperimentEventFactory;
import com.weichengcao.privadroid.util.RuntimePermissionAppUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.weichengcao.privadroid.sensors.SystemBroadcastReceiver.findPackageNameFromAppName;
import static com.weichengcao.privadroid.sensors.SystemBroadcastReceiver.getApplicationNameFromPackageName;
import static com.weichengcao.privadroid.sensors.SystemBroadcastReceiver.getApplicationVersion;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_CLASS_NAME;

public class AccessibilityEventMonitorService extends AccessibilityService {

    private static final String TAG = AccessibilityEventMonitorService.class.getSimpleName();

    private String currentlyHandledAppPackage;
    private String currentlyHandledAppName;
    private String currentlyHandledPermission;
    private String currentlyHandledAppVersion;
    private String currentlyInitiatedByUser;
    private String currentlyPermissionGranted;

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

    private PackageManager packageManager;

    /**
     * Runtime permission dialog texts
     */
    private static final String ALLOW_KEYWORD = "allow";
    private static final String DENY_KEYWORD = "deny";

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

        CharSequence packageName = event.getPackageName();
        int actionType = event.getAction();
        int eventType = event.getEventType();
        AccessibilityNodeInfo source = event.getSource();
        packageManager = PrivaDroidApplication.getAppContext().getPackageManager();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            switch (eventType) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    if (isPermissionsDialog(source)) {
                        extractInformationFromPermissionDialog(event);
                    }
                    break;
                case AccessibilityEvent.TYPE_VIEW_CLICKED:
                    if (isPermissionsDialogAction(source)) {
                        processPermissionDialogAction(source);
                    }
                    break;
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            Log.i(TAG, "Accessibility Event in N:24 version");
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            Log.i(TAG, "Accessibility Event in N_MR1:25 version");
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

    /**
     * Extract permission and app name from runtime permission request dialog.
     */
    private void extractInformationFromPermissionDialog(AccessibilityEvent event) {
        /**
         * Find the last active app package
         */
        currentlyHandledAppPackage = RuntimePermissionAppUtil.getLastActiveAppPackageName();

        /**
         * Extract permission name and app name from dialog text
         */
        for (CharSequence eventSubText : event.getText()) {
            Pattern permissionRegex = Pattern.compile("Allow (.*) to (.*)\\?");
            Matcher permissionMatcher = permissionRegex.matcher(eventSubText);
            if (permissionMatcher.find()) {
                String permissionText = permissionMatcher.group(2);
                currentlyHandledPermission = PERMISSION_DIALOG_STRINGS.get(permissionText);
                currentlyHandledAppName = permissionMatcher.group(1);

                // check if app name belongs to package name
                if (currentlyHandledAppPackage != null && currentlyHandledAppName != null &&
                        !currentlyHandledAppName.equals(getApplicationNameFromPackageName(currentlyHandledAppPackage, packageManager))) {
                    // TODO: change to better algo, currently compare app name to every package app name and find the right package name
                    currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, packageManager);
                }

                currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, packageManager);
                break;
            }
        }
    }

    /**
     * Extract grant/deny decision from runtime permission request dialog.
     */
    private void processPermissionDialogAction(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null) {
            return;
        }

        /**
         * Extract action option and send to Firestore
         */
        String actionTextLower = source.getText().toString().toLowerCase();
        if (actionTextLower.equals(ALLOW_KEYWORD)) {
            currentlyPermissionGranted = Boolean.toString(true);
        } else if (actionTextLower.equals(DENY_KEYWORD)) {
            currentlyPermissionGranted = Boolean.toString(false);
        }

        FirestoreProvider fp = new FirestoreProvider();
        fp.sendPermissionEvent(ExperimentEventFactory.createPermissionEvent(currentlyHandledAppName,
                currentlyHandledAppPackage, currentlyHandledAppVersion, currentlyHandledPermission,
                currentlyPermissionGranted, Boolean.toString(false)));
    }

    /**
     * Check if it's an action (deny/allow) in a runtime permission request dialog.
     */
    private boolean isPermissionsDialogAction(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null) {
            return false;
        }

        String nodeTextLowercase = source.getText().toString().toLowerCase();
        return source.getClassName().equals(BUTTON_CLASS_NAME) && (nodeTextLowercase.equals(ALLOW_KEYWORD) || nodeTextLowercase.equals(DENY_KEYWORD));
    }

    /**
     * Check if it's a runtime permission request dialog.
     */
    private boolean isPermissionsDialog(AccessibilityNodeInfo source) {
        return (source != null &&
                source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button").size() > 0);
    }

    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "Connected to the accessibility service");
    }

    @Override
    public void onInterrupt() {

    }
}

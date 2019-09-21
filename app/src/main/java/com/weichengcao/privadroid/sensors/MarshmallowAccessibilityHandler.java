package com.weichengcao.privadroid.sensors;

import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.AndroidSdkConstants;
import com.weichengcao.privadroid.util.ExperimentEventFactory;
import com.weichengcao.privadroid.util.RuntimePermissionAppUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.findPackageNameFromAppName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationNameFromPackageName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationVersion;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_CLASS_NAME;

public class MarshmallowAccessibilityHandler {

    private final static PackageManager packageManager = PrivaDroidApplication.getAppContext().getPackageManager();

    /**
     * Runtime permission dialog texts
     */
    private static final String ALLOW_KEYWORD = "allow";
    private static final String DENY_KEYWORD = "deny";

    /**
     * UI ids.
     */
    private static final String APP_NAME_ID_IN_APP_PERMISSIONS_SCREEN = "com.android.packageinstaller:id/name";

    private static String currentlyHandledAppPackage = null;
    private static String currentlyHandledAppName = null;
    private static String currentlyHandledPermission = null;
    private static String currentlyHandledAppVersion = null;
    private static String currentlyInitiatedByUser = null;
    private static String currentlyPermissionGranted = null;

    private static boolean insideSettingsAppListScreenOrChildren = false;
    private static boolean insideSettingsAppPermissionsScreen = false;

    public static void processAccessiblityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo source = event.getSource();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (isPermissionsDialog(source)) {
                    extractInformationFromPermissionDialog(event);
                } else if (isSettingsAppList(source)) {
                    insideSettingsAppListScreenOrChildren = true;

                    currentlyHandledAppName = null;
                    currentlyHandledAppPackage = null;
                    currentlyHandledAppVersion = null;
                    currentlyHandledPermission = null;
                    currentlyInitiatedByUser = null;
                    currentlyPermissionGranted = null;
                } else if (isSettingsAppPermissionsScreen(source)) {
                    insideSettingsAppPermissionsScreen = true;

                    currentlyHandledAppName = null;
                    currentlyHandledAppPackage = null;
                    currentlyHandledAppVersion = null;
                    currentlyHandledPermission = null;
                    currentlyInitiatedByUser = null;
                    currentlyPermissionGranted = null;

                    extractAppNameFromSettingsAppPermissionsScreen(source);
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (isPermissionsDialogAction(source)) {
                    processPermissionDialogAction(source);
                } else if (isTogglingPermissionInAppPermissionsScreen(source)) {
                    sendPermissionEventToFirebase(true);
                }
                break;
        }
    }

    /**
     * Detecting if user is effectively toggling permission toggle inside Settings -> Apps -> App info -> App permissions.
     */
    private static boolean isTogglingPermissionInAppPermissionsScreen(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppPermissionsScreen) {
            return false;
        }

        int childSize = source.getChildCount();
        for (int i = 0; i < childSize; i++) {
            AccessibilityNodeInfo child = source.getChild(i);
            if (child == null || child.getPackageName() == null || child.getText() == null) {
                continue;
            }
            String packageName = child.getPackageName().toString();
            String className = child.getClassName().toString();
            if ((packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE)
                    || packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE))
                    && className.equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME)) {
                currentlyHandledPermission = child.getText().toString();
            } else if ((packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE)
                    || packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE))
                    && className.equals(AndroidSdkConstants.SWITCH_CLASS_NAME)) {
                String switchStatus = child.getText().toString();
                if (switchStatus.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text).toLowerCase())) {
                    currentlyPermissionGranted = Boolean.toString(true);
                } else if (switchStatus.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_off_screen_text).toLowerCase())) {
                    currentlyPermissionGranted = Boolean.toString(false);
                }
            }

            /**
             * If we got both permission name and grant status.
             */
            if (currentlyHandledPermission != null && currentlyPermissionGranted != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extract app name from Settings -> Apps -> App info -> App permissions screen.
     */
    private static void extractAppNameFromSettingsAppPermissionsScreen(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppPermissionsScreen) {
            return;
        }

        List<AccessibilityNodeInfo> appNameNodes = source.findAccessibilityNodeInfosByViewId(APP_NAME_ID_IN_APP_PERMISSIONS_SCREEN);
        if (appNameNodes != null && appNameNodes.size() == 1) {
            AccessibilityNodeInfo appNameNode = appNameNodes.get(0);
            currentlyHandledAppName = appNameNode.getText().toString();

            /**
             * Get app package name and version from app name.
             */
            currentlyHandledAppPackage = AppPackagesBroadcastReceiver.findPackageNameFromAppName(currentlyHandledAppName, packageManager);
            currentlyHandledAppVersion = AppPackagesBroadcastReceiver.getApplicationVersion(currentlyHandledAppPackage, packageManager);
        }
    }

    /**
     * Detecting if we arrive at the "App permissions" screen containing permission settings of an app.
     */
    private static boolean isSettingsAppPermissionsScreen(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppListScreenOrChildren) {
            return false;
        }

        int childCount = source.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = source.getChild(i);
            if (child == null || child.getClassName() == null || child.getPackageName() == null) {
                continue;
            }
            String packageName = child.getPackageName().toString();
            String className = child.getClassName().toString();
            if ((packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE) ||
                    packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE))
                    && className.equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME)) {
                if (child.getText() == null) {
                    continue;
                }
                String text = child.getText().toString();
                if (text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.app_permissions_screen_text).toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Detecting if we arrive at the "Apps" screen containing a list of all apps.
     */
    private static boolean isSettingsAppList(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        int childCount = source.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = source.getChild(i);
            if (child == null || child.getPackageName() == null || child.getClassName() == null) {
                continue;
            }
            String packageName = child.getPackageName().toString();
            String className = child.getClassName().toString();
            if (packageName.equals(AndroidSdkConstants.SETTINGS_PACKAGE) && className.equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME)) {
                if (child.getText() == null) {
                    continue;
                }
                String text = child.getText().toString();
                if (text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.apps_screen_text).toLowerCase())) {
                    insideSettingsAppListScreenOrChildren = true;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extract permission and app name from runtime permission request dialog.
     */
    private static void extractInformationFromPermissionDialog(AccessibilityEvent event) {
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
                currentlyHandledPermission = AccessibilityEventMonitorService.PERMISSION_DIALOG_STRINGS.get(permissionText);
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

    private static void sendPermissionEventToFirebase(boolean initiatedByUser) {
        if (currentlyPermissionGranted == null || currentlyPermissionGranted.isEmpty()
                || currentlyHandledPermission == null || currentlyHandledPermission.isEmpty()
                || currentlyHandledAppVersion == null || currentlyHandledAppVersion.isEmpty()
                || currentlyHandledAppPackage == null || currentlyHandledAppPackage.isEmpty()
                || currentlyHandledAppName == null || currentlyHandledAppName.isEmpty()) {
            return;
        }

        FirestoreProvider firestoreProvider = new FirestoreProvider();
        firestoreProvider.sendPermissionEvent(ExperimentEventFactory.createPermissionEvent(currentlyHandledAppName,
                currentlyHandledAppPackage, currentlyHandledAppVersion, currentlyHandledPermission,
                currentlyPermissionGranted, Boolean.toString(initiatedByUser)));
    }

    /**
     * Extract grant/deny decision from runtime permission request dialog.
     */
    private static void processPermissionDialogAction(AccessibilityNodeInfo source) {
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

        sendPermissionEventToFirebase(false);
    }

    /**
     * Check if it's an action (deny/allow) in a runtime permission request dialog.
     */
    private static boolean isPermissionsDialogAction(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null) {
            return false;
        }

        String nodeTextLowercase = source.getText().toString().toLowerCase();
        return source.getClassName().equals(BUTTON_CLASS_NAME) && (nodeTextLowercase.equals(ALLOW_KEYWORD) || nodeTextLowercase.equals(DENY_KEYWORD));
    }

    /**
     * Check if it's a runtime permission request dialog.
     */
    private static boolean isPermissionsDialog(AccessibilityNodeInfo source) {
        return (source != null &&
                source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button").size() > 0);
    }
}

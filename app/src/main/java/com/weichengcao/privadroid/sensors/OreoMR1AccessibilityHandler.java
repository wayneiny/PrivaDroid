package com.weichengcao.privadroid.sensors;

import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.AndroidSdkConstants;
import com.weichengcao.privadroid.util.ExperimentEventFactory;
import com.weichengcao.privadroid.util.RuntimePermissionAppUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.findPackageNameFromAppName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationNameFromPackageName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationVersion;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.TEXTVIEW_CLASS_NAME;

public class OreoMR1AccessibilityHandler {

    private final static String TAG = MarshmallowAccessibilityHandler.class.getSimpleName();

    private final static PackageManager packageManager = PrivaDroidApplication.getAppContext().getPackageManager();

    private static String currentlyHandledAppPackage = null;
    private static String currentlyHandledAppName = null;
    private static String currentlyHandledPermission = null;
    private static String currentlyHandledAppVersion = null;
    private static String currentlyInitiatedByUser = null;
    private static String currentlyPermissionGranted = null;

    /**
     * If we are inside the screen where all permission settings are shown for a single app.
     */
    private static boolean insideSettingsAppPermissionsForAnAppScreen = false;

    /**
     * If we are inside the screen of a single app info.
     */
    private static boolean insideAppInfoOfSingleAppScreen = false;

    /**
     * This screen represents a single permission and all the apps that ask for this permission and
     * their switch status.
     */
    private static boolean insideSinglePermissionToAllAppsSettingsScreen = false;

    private static HashMap<String, String> permissionNames2permissionSwitchStatus = new HashMap<>();

    /**
     * In single permission to all apps screen, record the app to status mappings.
     */
    private static HashMap<String, String> appNames2PermissionSwitchStatus = new HashMap<>();

    public static void processAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo source = event.getSource();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (isPermissionsDialog(source)) {
                    Log.d(TAG, "We are in a runtime permission dialog.");
                    extractInformationFromPermissionDialog(event);
                } else if (insideAppInfoOfSingleAppScreen && isSettingsAppPermissionsScreen(source)) {
                    Log.d(TAG, "We are in the App permissions screen.");
                    insideSettingsAppPermissionsForAnAppScreen = true;

                    extractAppNameFromSettingsAppPermissionsScreenAndRecordCurrentPermissionSettings(source);
                } else if (isAppsAndNotificationsScreen(source)) {
                    Log.d(TAG, "We are in the Settings -> Apps & notifications screen.");
                    insideAppInfoOfSingleAppScreen = false;
                    insideSettingsAppPermissionsForAnAppScreen = false;
                    insideSinglePermissionToAllAppsSettingsScreen = false;

                    currentlyHandledAppName = null;
                    currentlyHandledAppPackage = null;
                    currentlyHandledAppVersion = null;
                    currentlyHandledPermission = null;
                    currentlyInitiatedByUser = null;
                    currentlyPermissionGranted = null;
                } else if (isSinglePermissionToAllAppsScreen(source)) {
                    Log.d(TAG, "We are in a single permission to all apps screen.");
                    insideSinglePermissionToAllAppsSettingsScreen = true;
                    extractAppNameToSinglePermissionSwitchStatus(source);
                } else if (isAppInfoOfSingleAppScreen(source)) {
                    Log.d(TAG, "We are in App info (list of all apps) screen.");
                    insideAppInfoOfSingleAppScreen = true;
                    permissionNames2permissionSwitchStatus = new HashMap<>();
                } else if (isPermissionsCategoriesToAppsScreen(source)) {
                    Log.d(TAG, "We are in App permissions (list of all permission) in window content change.");
                    insideSinglePermissionToAllAppsSettingsScreen = false;
                    appNames2PermissionSwitchStatus = new HashMap<>();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (insideSettingsAppPermissionsForAnAppScreen && permissionNames2permissionSwitchStatus != null && !permissionNames2permissionSwitchStatus.isEmpty()) {
                    findDifferenceBetweenPermissionNameToSwitchStatusAndSendEvents(source);
                } else if (insideSinglePermissionToAllAppsSettingsScreen && appNames2PermissionSwitchStatus != null && !appNames2PermissionSwitchStatus.isEmpty()) {
                    findDifferenceBetweenAppNameToSwitchStatusAndSendEvents(source);
                    extractAppNameToSinglePermissionSwitchStatus(source);
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (isPermissionsDialogAction(source)) {
                    Log.d(TAG, "We acted in a runtime permission request dialog.");
                    processPermissionDialogAction(source);

                    sendPermissionEventToFirebase(false);
                }
                break;
        }
    }

    private static void findDifferenceBetweenAppNameToSwitchStatusAndSendEvents(AccessibilityNodeInfo source) {
        // record any difference between current and stored values
        if (source != null && source.getClassName() != null && source.getClassName().toString().equals("android.widget.ListView")) {
            Log.d(TAG, "Looking for difference between previous permission2status map and updated one.");
            // A permission settings list view change
            int childCount = source.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = source.getChild(i);
                if (child == null || child.getClassName() == null || child.getPackageName() == null || child.getChildCount() != 2) {
                    continue;
                }
                AccessibilityNodeInfo appName = child.getChild(0);
                AccessibilityNodeInfo appSwitch = child.getChild(1);
                if (appName == null || appSwitch == null) {
                    continue;
                }
                String name = appName.getText().toString();
                String status = appSwitch.getText().toString();
                if (appNames2PermissionSwitchStatus.get(name) != null &&
                        !appNames2PermissionSwitchStatus.get(name).equals(status)) {
                    // we found a difference
                    currentlyPermissionGranted = Boolean.toString(status.toLowerCase()
                            .equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text).toLowerCase()));
                    currentlyHandledAppName = name;
                    currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, PrivaDroidApplication.getAppContext().getPackageManager());
                    currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, PrivaDroidApplication.getAppContext().getPackageManager());
                    sendPermissionEventToFirebase(true);
                    // update
                    appNames2PermissionSwitchStatus.put(name, status);
                }
            }
        }
    }

    private static boolean isPermissionsCategoriesToAppsScreen(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        boolean foundAppPermissions = false;
        boolean foundCamera = false;
        boolean foundBodySensors = false;

        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        while (!allChildren.isEmpty()) {
            AccessibilityNodeInfo cur = allChildren.poll();
            if (cur != null) {
                int childCount = cur.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    allChildren.add(cur.getChild(i));
                }

                if (cur.getClassName() != null && cur.getClassName().toString().toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase())
                        && cur.getText() != null && cur.getText().toString().toLowerCase().equals(PrivaDroidApplication.getAppContext().getResources().getString(R.string.android_camera_screen_text).toLowerCase())) {
                    foundCamera = true;
                } else if (cur.getClassName() != null && cur.getClassName().toString().toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase())
                        && cur.getText() != null && cur.getText().toString().toLowerCase().equals(PrivaDroidApplication.getAppContext().getResources().getString(R.string.android_body_sensors_screen_text).toLowerCase())) {
                    foundBodySensors = true;
                } else if (cur.getClassName() != null && cur.getClassName().toString().toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase())
                        && cur.getText() != null && cur.getText().toString().toLowerCase().equals(PrivaDroidApplication.getAppContext().getResources().getString(R.string.android_o_app_permissions_screen_text).toLowerCase())) {
                    foundAppPermissions = true;
                }
            }
        }

        return foundAppPermissions && foundCamera && foundBodySensors;
    }

    private static void findDifferenceBetweenPermissionNameToSwitchStatusAndSendEvents(AccessibilityNodeInfo source) {
        // record any difference between current and stored values
        if (source != null && source.getClassName() != null && source.getClassName().toString().equals("android.widget.ListView")) {
            Log.d(TAG, "Looking for difference between previous permission2status map and updated one.");
            // A permission settings list view change
            int childCount = source.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = source.getChild(i);
                if (child == null || child.getClassName() == null || child.getPackageName() == null || child.getChildCount() != 2) {
                    continue;
                }
                AccessibilityNodeInfo permissionName = child.getChild(0);
                AccessibilityNodeInfo appSwitch = child.getChild(1);
                if (permissionName == null || appSwitch == null) {
                    continue;
                }
                String name = permissionName.getText().toString();
                String status = appSwitch.getText().toString();
                if (permissionNames2permissionSwitchStatus.containsKey(name) &&
                        permissionNames2permissionSwitchStatus.get(name) != null &&
                        !permissionNames2permissionSwitchStatus.get(name).equals(status)) {
                    // we found a difference
                    currentlyPermissionGranted = Boolean.toString(status.toLowerCase()
                            .equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text).toLowerCase()));
                    currentlyHandledPermission = name;
                    sendPermissionEventToFirebase(true);
                    // update
                    permissionNames2permissionSwitchStatus.put(name, status);
                }
            }
        }
    }

    /**
     * Detect if we are in App info (list of all app) screen.
     */
    private static boolean isAppInfoOfSingleAppScreen(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        boolean foundAppInfo = false;
        boolean foundPermissions = false;
        boolean foundStorage = false;

        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        while (!allChildren.isEmpty()) {
            AccessibilityNodeInfo cur = allChildren.poll();

            if (cur != null) {
                int childCount = cur.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    allChildren.add(cur.getChild(i));
                }

                if (cur.getClassName() != null && cur.getClassName().toString().toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase())
                        && cur.getText() != null && cur.getText().toString().toLowerCase().equals(PrivaDroidApplication.getAppContext().getResources().getString(R.string.android_o_app_info_screen_text).toLowerCase())) {
                    foundAppInfo = true;
                } else if (cur.getClassName() != null && cur.getClassName().toString().toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase())
                        && cur.getText() != null && cur.getText().toString().toLowerCase().equals(PrivaDroidApplication.getAppContext().getResources().getString(R.string.android_permissions_screen_text).toLowerCase())) {
                    foundPermissions = true;
                } else if (cur.getClassName() != null && cur.getClassName().toString().toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase())
                        && cur.getText() != null && cur.getText().toString().toLowerCase().equals(PrivaDroidApplication.getAppContext().getResources().getString(R.string.android_storage_screen_text).toLowerCase())) {
                    foundStorage = true;
                }
            }
        }

        return foundStorage && foundAppInfo && foundPermissions;
    }

    /**
     * Extract app names to single permission access mappings.
     */
    private static void extractAppNameToSinglePermissionSwitchStatus(AccessibilityNodeInfo source) {
        if (source == null) {
            return;
        }

        List<AccessibilityNodeInfo> appLists = source.findAccessibilityNodeInfosByViewId("android:id/list");
        if (appLists != null && !appLists.isEmpty()) {
            AccessibilityNodeInfo listView = appLists.get(0);

            int appCount = listView.getChildCount();
            for (int i = 0; i < appCount; i++) {
                AccessibilityNodeInfo appLayout = listView.getChild(i);
                if (appLayout == null || appLayout.getChildCount() != 2) {
                    continue;
                }

                AccessibilityNodeInfo appName = appLayout.getChild(0);
                AccessibilityNodeInfo appSwitch = appLayout.getChild(1);

                if (appName == null || appName.getText() == null || !appName.getClassName().toString().toLowerCase().equals("android.widget.TextView".toLowerCase())
                        || appSwitch == null || appSwitch.getText() == null || !appSwitch.getClassName().toString().toLowerCase().equals("android.widget.Switch".toLowerCase())) {
                    continue;
                }

                String appNameText = appName.getText().toString();
                String switchStatus = appSwitch.getText().toString();

                if (appNames2PermissionSwitchStatus != null && !appNames2PermissionSwitchStatus.isEmpty()) {
                    if (appNames2PermissionSwitchStatus.get(appNameText) != null &&
                            !appNames2PermissionSwitchStatus.get(appNameText).equals(switchStatus)) {
                        currentlyHandledAppName = appNameText;
                        currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, PrivaDroidApplication.getAppContext().getPackageManager());
                        currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, PrivaDroidApplication.getAppContext().getPackageManager());
                        currentlyPermissionGranted = Boolean.toString(switchStatus.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text).toLowerCase()));
                        sendPermissionEventToFirebase(true);
                    }
                } else {
                    appNames2PermissionSwitchStatus = new HashMap<>();
                }
                appNames2PermissionSwitchStatus.put(appNameText, switchStatus);
            }
        }
    }

    /**
     * Detect if we are in the single permission to all apps screen.
     */
    private static boolean isSinglePermissionToAllAppsScreen(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        String[] permissionCategories = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.android_permission_categories);

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
                    && className.equals(TEXTVIEW_CLASS_NAME)) {
                if (child.getText() == null) {
                    continue;
                }
                String text = child.getText().toString();
                for (String p : permissionCategories) {
                    if (text.toLowerCase().contains(p.toLowerCase())) {
                        currentlyHandledPermission = p;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Detect if we are in "Apps & notifications" screen.
     */
    private static boolean isAppsAndNotificationsScreen(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        boolean foundAppsAndNotifications = false;
        boolean foundAppPermissions = false;

        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        while (!allChildren.isEmpty()) {
            AccessibilityNodeInfo cur = allChildren.poll();

            if (cur != null) {
                int childCount = cur.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    allChildren.add(cur.getChild(i));
                }

                if (cur.getText() != null && cur.getClassName() != null) {
                    String text = cur.getText().toString();
                    String className = cur.getClassName().toString();
                    if (className.toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase()) &&
                            text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.android_o_apps_and_notifications_screen_text).toLowerCase())) {
                        foundAppsAndNotifications = true;
                    } else if (className.toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase()) &&
                            text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.app_permissions_screen_text).toLowerCase())) {
                        foundAppPermissions = true;
                    }
                }
            }
        }

        return foundAppsAndNotifications && foundAppPermissions;
    }

    private static void extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(AccessibilityEvent event, boolean isFirstPermissionRequest) {
        for (CharSequence eventSubText : event.getText()) {
            Pattern permissionRegex = Pattern.compile("Allow (.*) to (.*)\\?");
            Matcher permissionMatcher = permissionRegex.matcher(eventSubText);
            if (permissionMatcher.find()) {
                String permissionText = permissionMatcher.group(2);
                if (isFirstPermissionRequest) {
                    currentlyHandledPermission = AccessibilityEventMonitorService.PERMISSION_DIALOG_STRINGS.get(permissionText);
                } else {
                }
                currentlyHandledAppName = permissionMatcher.group(1);

                // check if app name belongs to package name
                if (currentlyHandledAppPackage != null && currentlyHandledAppName != null &&
                        !currentlyHandledAppName.equals(getApplicationNameFromPackageName(currentlyHandledAppPackage, packageManager))) {
                    // NOTE: change to better algo, currently compare app name to every package app name and find the right package name
                    currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, packageManager);
                }

                currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, packageManager);
                break;
            }
        }
    }

    /**
     * Extract app name from Settings -> Apps -> App info -> App permissions screen and record the
     * current permission settings.
     */
    private static void extractAppNameFromSettingsAppPermissionsScreenAndRecordCurrentPermissionSettings(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppPermissionsForAnAppScreen) {
            return;
        }

        List<AccessibilityNodeInfo> appNameNodes = source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name");
        if (appNameNodes != null && appNameNodes.size() == 1) {
            AccessibilityNodeInfo appNameNode = appNameNodes.get(0);
            currentlyHandledAppName = appNameNode.getText().toString();

            /**
             * Get app package name and version from app name.
             */
            currentlyHandledAppPackage = AppPackagesBroadcastReceiver.findPackageNameFromAppName(currentlyHandledAppName, packageManager);
            currentlyHandledAppVersion = AppPackagesBroadcastReceiver.getApplicationVersion(currentlyHandledAppPackage, packageManager);
        }

        /**
         * NOTE: Record the permission settings for later permission deny warning use.
         */
        List<AccessibilityNodeInfo> permissionSettingsListView = source.findAccessibilityNodeInfosByViewId("android:id/list");
        if (permissionSettingsListView != null && !permissionSettingsListView.isEmpty()) {
            AccessibilityNodeInfo listView = permissionSettingsListView.get(0);
            int childCount = listView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = listView.getChild(i);
                if (child == null || child.getPackageName() == null || child.getClassName() == null) {
                    continue;
                }
                /**
                 * This level is the LinearLayout for a permission name and its switch.
                 */
                List<AccessibilityNodeInfo> permissionTitles = child.findAccessibilityNodeInfosByViewId("android:id/title");
                AccessibilityNodeInfo permissionTitleNode = null;
                if (permissionTitles != null && !permissionTitles.isEmpty()) {
                    permissionTitleNode = permissionTitles.get(0);
                }
                List<AccessibilityNodeInfo> switchTexts = child.findAccessibilityNodeInfosByViewId("android:id/switch_widget");
                AccessibilityNodeInfo switchTextNode = null;
                if (switchTexts != null && !switchTexts.isEmpty()) {
                    switchTextNode = switchTexts.get(0);
                }
                if (permissionTitleNode != null && permissionTitleNode.getText() != null && switchTextNode != null && switchTextNode.getText() != null) {
                    if (permissionNames2permissionSwitchStatus != null && !permissionNames2permissionSwitchStatus.isEmpty()) {
                        if (permissionNames2permissionSwitchStatus.containsKey(permissionTitleNode.getText().toString()) &&
                                permissionNames2permissionSwitchStatus.get(permissionTitleNode.getText().toString()) != null &&
                                !permissionNames2permissionSwitchStatus.get(permissionTitleNode.getText().toString()).equals(switchTextNode.getText().toString())) {
                            currentlyHandledPermission = permissionTitleNode.getText().toString();
                            currentlyPermissionGranted = Boolean.toString(switchTextNode.getText().toString().toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text)));
                            sendPermissionEventToFirebase(true);
                        }
                    } else {
                        permissionNames2permissionSwitchStatus = new HashMap<>();
                    }
                    permissionNames2permissionSwitchStatus.put(permissionTitleNode.getText().toString(), switchTextNode.getText().toString());
                }
            }
        }
    }

    /**
     * Detecting if we arrive at the "App permissions" screen containing permission settings of an app.
     */
    private static boolean isSettingsAppPermissionsScreen(AccessibilityNodeInfo source) {
        if (source == null) {
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
                    && className.equals(TEXTVIEW_CLASS_NAME)) {
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
        extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(event, true);
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

        currentlyPermissionGranted = null;
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
        if (actionTextLower.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_screen_text).toLowerCase())) {
            currentlyPermissionGranted = Boolean.toString(true);
        } else if (actionTextLower.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text).toLowerCase())) {
            currentlyPermissionGranted = Boolean.toString(false);
        }
    }

    /**
     * Check if it's an action (deny/allow) in a runtime permission request dialog.
     */
    private static boolean isPermissionsDialogAction(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null) {
            return false;
        }

        String nodeTextLowercase = source.getText().toString().toLowerCase();
        return source.getClassName().equals(BUTTON_CLASS_NAME) &&
                (nodeTextLowercase.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_screen_text).toLowerCase()) ||
                        nodeTextLowercase.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text).toLowerCase()));
    }

    /**
     * Check if it's a runtime permission request dialog.
     */
    private static boolean isPermissionsDialog(AccessibilityNodeInfo source) {
        return (source != null &&
                source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button").size() > 0);
    }
}

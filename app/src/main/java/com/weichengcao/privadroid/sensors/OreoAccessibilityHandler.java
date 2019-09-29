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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.findPackageNameFromAppName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationNameFromPackageName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationVersion;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_SHORTHAND;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.LISTVIEW_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.SWITCH_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.TEXTVIEW_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.TEXTVIEW_SHORTHAND;

class OreoAccessibilityHandler {

    private final static String TAG = OreoAccessibilityHandler.class.getSimpleName();

    private final static PackageManager packageManager = PrivaDroidApplication.getAppContext().getPackageManager();

    private static String currentlyHandledAppPackage = null;
    private static String currentlyHandledAppName = null;
    private static String currentlyHandledPermission = null;
    private static String currentlyHandledSubsequentPermission = null;
    private static String currentlyHandledAppVersion = null;
    private static String currentlyPermissionGranted = null;
    private static String currentlyProactivePermissionRequestRationale = null;
    private static String currentlyProactivePermissionRequestRationaleGranted = null;
    private static String currentlyProactivePermissionRequestEventCorrelationId = null;

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

    private static boolean runIntoAppProactivePermissionRequestDialog = false;

    private static HashMap<String, String> permissionNames2permissionSwitchStatus = new HashMap<>();

    /**
     * In single permission to all apps screen, record the app to status mappings.
     */
    private static HashMap<String, String> appNames2PermissionSwitchStatus = new HashMap<>();

    static void processAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo source = event.getSource();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (isPermissionsDialog(source)) {
//                    Log.d(TAG, "We are in a runtime permission dialog.");
                    extractInformationFromPermissionDialog(event);
                } else if (insideAppInfoOfSingleAppScreen && isSettingsAppPermissionsScreen(source)) {
//                    Log.d(TAG, "We are in the App permissions screen.");
                    insideSettingsAppPermissionsForAnAppScreen = true;

                    if (!permissionNames2permissionSwitchStatus.isEmpty()) {
                        findDifferenceBetweenPermissionNameToSwitchStatusAndSendEvents(source);
                    }

                    extractAppNameFromSettingsAppPermissionsScreenAndRecordCurrentPermissionSettings(source);
                } else if (isAppsAndNotificationsScreen(source)) {
//                    Log.d(TAG, "We are in the Settings -> Apps & notifications screen.");
                    insideAppInfoOfSingleAppScreen = false;
                    insideSettingsAppPermissionsForAnAppScreen = false;
                    insideSinglePermissionToAllAppsSettingsScreen = false;

                    currentlyHandledAppName = null;
                    currentlyHandledAppPackage = null;
                    currentlyHandledAppVersion = null;
                    currentlyHandledPermission = null;
                    currentlyHandledSubsequentPermission = null;
                    currentlyPermissionGranted = null;
                } else if (isSinglePermissionToAllAppsScreen(source)) {
//                    Log.d(TAG, "We are in a single permission to all apps screen.");
                    insideSinglePermissionToAllAppsSettingsScreen = true;

                    if (!appNames2PermissionSwitchStatus.isEmpty()) {
                        findDifferenceBetweenAppNameToSwitchStatusAndSendEvents(source);
                    }

                    extractAppNameToSinglePermissionSwitchStatus(source);
                } else if (isAppInfoOfSingleAppScreen(source)) {
//                    Log.d(TAG, "We are in App info (list of all apps) screen.");
                    insideAppInfoOfSingleAppScreen = true;
                    permissionNames2permissionSwitchStatus = new HashMap<>();
                } else if (isPermissionsCategoriesToAppsScreen(source)) {
//                    Log.d(TAG, "We are in App permissions (list of all permission) in window content change.");
                    insideSinglePermissionToAllAppsSettingsScreen = false;
                    appNames2PermissionSwitchStatus = new HashMap<>();
                } else if (isAppProactivePermissionRequest(source)) {
//                    Log.d(TAG, "We encountered an app proactive permission dialog.");
                    runIntoAppProactivePermissionRequestDialog = true;

                    extractRationaleMessageFromProactivePermissionRequest(source);
                } else {
//                    Log.d(TAG, "Unhandled TYPE_WINDOW_STATE_CHANGED event");
                }
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                /**
                 * NOTE: A consecutive second permission request dialog happens.
                 */
                processConsecutivePermissionRequestByAnApp(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (insideSettingsAppPermissionsForAnAppScreen) {
                    if (!permissionNames2permissionSwitchStatus.isEmpty()) {
                        findDifferenceBetweenPermissionNameToSwitchStatusAndSendEvents(source);
                    }
                    extractAppNameFromSettingsAppPermissionsScreenAndRecordCurrentPermissionSettings(source);
                } else if (insideSinglePermissionToAllAppsSettingsScreen) {
                    if (!appNames2PermissionSwitchStatus.isEmpty()) {
                        findDifferenceBetweenAppNameToSwitchStatusAndSendEvents(source);
                    }
                    extractAppNameToSinglePermissionSwitchStatus(source);
                } else if (isPermissionsDialog(source)) {
//                    Log.d(TAG, "We are in a runtime permission dialog.");
                    extractInformationFromPermissionDialog(event);
                } else {
//                    Log.d(TAG, "Unhandled TYPE_WINDOW_CONTENT_CHANGED event");
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (isPermissionsDialogAction(source)) {
//                    Log.d(TAG, "We acted in a runtime permission request dialog.");
                    processPermissionDialogAction(source);

                    sendPermissionEventToFirebase(false);
                } else if (isClickingInProactivePermissionRequestDialog(source, event)) {
//                    Log.d(TAG, "Detected a click in proactive permission request dialog.");
                    processProactivePermissionRequestDialogAction(source, event);

                    sendProactivePermissionRequestEventToFirebase();
                } else {
//                    Log.d(TAG, "Unhandled TYPE_VIEW_CLICKED event");
                }
                break;
        }
    }

    //region Proactive permission requests

    /**
     * Process and extract the decision of proactive permission request.
     */
    private static void processProactivePermissionRequestDialogAction(AccessibilityNodeInfo source, AccessibilityEvent event) {
        HashSet<String> proactivePermissionGrantButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_grant_button_texts)));
        HashSet<String> proactivePermissionDenyButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_deny_button_texts)));

        String className = null;
        String nodeTextLowercase = null;
        if (source != null && (source.getText() != null || source.getContentDescription() != null) &&
                source.getClassName() != null && runIntoAppProactivePermissionRequestDialog) {
            nodeTextLowercase = null;
            if (source.getText() != null) {
                nodeTextLowercase = source.getText().toString().toLowerCase();
            } else if (source.getContentDescription() != null) {
                nodeTextLowercase = source.getContentDescription().toString().toLowerCase();
            }
            className = source.getClassName().toString();
        } else if (event != null && event.getText() != null && event.getText().size() > 0 && event.getClassName() != null
                && runIntoAppProactivePermissionRequestDialog) {
            nodeTextLowercase = event.getText().get(0).toString().toLowerCase();
            className = event.getClassName().toString();
        }

        if (className != null && nodeTextLowercase != null) {
            if (className.equals(BUTTON_CLASS_NAME)) {
                if (proactivePermissionGrantButtonTexts.contains(nodeTextLowercase)) {
                    currentlyProactivePermissionRequestRationaleGranted = Boolean.toString(true);
//                    Log.d(TAG, "Detected user click grant related button in proactive permission request dialog.");
                } else if (proactivePermissionDenyButtonTexts.contains(nodeTextLowercase)) {
                    currentlyProactivePermissionRequestRationaleGranted = Boolean.toString(false);
//                    Log.d(TAG, "Detected user click deny related button in proactive permission request dialog.");
                }
            }
        }
    }

    /**
     * Detect if a click happens in proactive permission request dialog.
     */
    private static boolean isClickingInProactivePermissionRequestDialog(AccessibilityNodeInfo source, AccessibilityEvent event) {
        HashSet<String> proactivePermissionGrantButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_grant_button_texts)));
        HashSet<String> proactivePermissionDenyButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_deny_button_texts)));

        String className;
        String nodeTextLowercase = null;
        if (source != null && (source.getText() != null || source.getContentDescription() != null) &&
                source.getClassName() != null && runIntoAppProactivePermissionRequestDialog) {
            if (source.getText() != null) {
                nodeTextLowercase = source.getText().toString().toLowerCase();
            } else if (source.getContentDescription() != null) {
                nodeTextLowercase = source.getContentDescription().toString().toLowerCase();
            }
            className = source.getClassName().toString();
            return className.equals(BUTTON_CLASS_NAME) && nodeTextLowercase != null &&
                    (proactivePermissionGrantButtonTexts.contains(nodeTextLowercase) ||
                            proactivePermissionDenyButtonTexts.contains(nodeTextLowercase));
        } else if (event != null && event.getText() != null && event.getText().size() > 0 && event.getClassName() != null
                && runIntoAppProactivePermissionRequestDialog) {
            nodeTextLowercase = event.getText().get(0).toString().toLowerCase();
            className = event.getClassName().toString();
            return className.equals(BUTTON_CLASS_NAME) &&
                    (proactivePermissionGrantButtonTexts.contains(nodeTextLowercase) ||
                            proactivePermissionDenyButtonTexts.contains(nodeTextLowercase));
        }

        return false;
    }

    /**
     * Extract rationale message from proactive permission request.
     */
    private static void extractRationaleMessageFromProactivePermissionRequest(AccessibilityNodeInfo source) {
        if (source == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        while (!allChildren.isEmpty()) {
            AccessibilityNodeInfo cur = allChildren.poll();
            if (cur != null) {
                int childCount = cur.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    allChildren.add(cur.getChild(i));
                }

                if (cur.getClassName() != null && cur.getText() != null) {
                    if (cur.getClassName().toString().equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME)) {
                        sb.append(TEXTVIEW_SHORTHAND).append(": ")
                                .append(cur.getText().toString()).append(" ");
                    } else if (cur.getClassName().toString().equals(BUTTON_CLASS_NAME)) {
                        sb.append(BUTTON_SHORTHAND).append(": ")
                                .append(cur.getText().toString()).append(". ");
                    }
                }
            }
        }

        currentlyProactivePermissionRequestRationale = sb.toString();
//        Log.d(TAG, "Extracted proactive permission request rationale to be " + currentlyProactivePermissionRequestRationale);

        /**
         * Find the last active app package
         */
        if (source.getPackageName() != null) {
            currentlyHandledAppPackage = source.getPackageName().toString();
            currentlyHandledAppName = getApplicationNameFromPackageName(currentlyHandledAppPackage, packageManager);
            currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, packageManager);
//            Log.d(TAG, "Extracted app package from app provided permission request rationale to be " + currentlyHandledAppPackage);
//            Log.d(TAG, "Extracted app name from app provided permission request rationale to be " + currentlyHandledAppName);
//            Log.d(TAG, "Extracted app version from app provided permission request rationale to be " + currentlyHandledAppVersion);
        }
    }

    /**
     * Detect if it's an application proactive permission request message/dialog.
     * Proactive permission request message/dialog is a dialog asking if users agree to grant access
     * to an app before the actual permission request.
     */
    private static boolean isAppProactivePermissionRequest(AccessibilityNodeInfo source) {
        if (source == null) {
//            Log.d(TAG, "source is null");
            return false;
        }

        /**
         * Check package name first. If it's Android system dialog, return false.
         */
        if (source.getPackageName() != null) {
            String packageName = source.getPackageName().toString();
            if (packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE) ||
                    packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE)) {
                return false;
            }
        }

        boolean foundRationaleKeywords = false;
        boolean foundButtons = false;

        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        while (!allChildren.isEmpty()) {
            AccessibilityNodeInfo cur = allChildren.poll();
            if (cur != null) {
                int childCount = cur.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    allChildren.add(cur.getChild(i));
                }

                if (cur.getClassName() != null && cur.getClassName().toString().equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME)
                        && cur.getText() != null) {
                    String textLowerCase = cur.getText().toString().toLowerCase();
                    for (String s : AccessibilityEventMonitorService.PERMISSION_RELATED_KEYWORDS) {
                        if (textLowerCase.contains(s)) {
                            foundRationaleKeywords = true;
//                            Log.d(TAG, "Found rationale " + s + " keyword in potential proactive permission dialog.");
                            break;
                        }
                    }
                } else if (cur.getClassName() != null && cur.getClassName().toString().equals(BUTTON_CLASS_NAME)) {
                    String textLowerCase = null;
                    if (cur.getText() != null) {
                        textLowerCase = cur.getText().toString().toLowerCase();
                    } else if (cur.getContentDescription() != null) {
                        textLowerCase = cur.getContentDescription().toString().toLowerCase();
                    }
                    if (AccessibilityEventMonitorService.PERMISSION_RATIONALE_BUTTON_KEYWORDS.contains(textLowerCase)) {
                        foundButtons = true;
//                        Log.d(TAG, "Found button " + textLowerCase + " keyword in potential proactive permission dialog.");
                    }
                }
            }
        }

        return foundButtons && foundRationaleKeywords;
    }
    //endregion

    //region Settings screen detection

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
                    boolean isTextview = className.toLowerCase().equals(TEXTVIEW_CLASS_NAME.toLowerCase());
                    if (isTextview && text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.android_o_apps_and_notifications_screen_text).toLowerCase())) {
                        foundAppsAndNotifications = true;
                    } else if (isTextview && text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.app_permissions_screen_text).toLowerCase())) {
                        foundAppPermissions = true;
                    }
                }
            }
        }

        return foundAppsAndNotifications && foundAppPermissions;
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
    //endregion

    //region Permission to app list

    private static void findDifferenceBetweenAppNameToSwitchStatusAndSendEvents(AccessibilityNodeInfo source) {
        // record any difference between current and stored values
        if (source != null && source.getClassName() != null && source.getClassName().toString().equals(LISTVIEW_CLASS_NAME)) {
//            Log.d(TAG, "Looking for difference between previous app2status map and updated one.");
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
                if (appNames2PermissionSwitchStatus.get(name) != null && !Objects.equals(appNames2PermissionSwitchStatus.get(name), status)) {
//                    Log.d(TAG, "Found difference in " + name + " and permission " + status);
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

                if (appName == null || appName.getText() == null || !appName.getClassName().toString().equals(TEXTVIEW_CLASS_NAME)
                        || appSwitch == null || appSwitch.getText() == null || !appSwitch.getClassName().toString().equals(SWITCH_CLASS_NAME)) {
                    continue;
                }

                String appNameText = appName.getText().toString();
                String switchStatus = appSwitch.getText().toString();

                if (appNames2PermissionSwitchStatus != null && !appNames2PermissionSwitchStatus.isEmpty()) {
                    if (appNames2PermissionSwitchStatus.get(appNameText) != null &&
                            !Objects.equals(appNames2PermissionSwitchStatus.get(appNameText), switchStatus)) {
                        currentlyHandledAppName = appNameText;
                        currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, PrivaDroidApplication.getAppContext().getPackageManager());
                        currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, PrivaDroidApplication.getAppContext().getPackageManager());
                        currentlyPermissionGranted = Boolean.toString(switchStatus.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text).toLowerCase()));
                        sendPermissionEventToFirebase(true);
                    }
                } else {
                    appNames2PermissionSwitchStatus = new HashMap<>();
                }
//                Log.d(TAG, "Logged " + appNameText + " to " + switchStatus + " in " + currentlyHandledPermission + " to app screen.");
                appNames2PermissionSwitchStatus.put(appNameText, switchStatus);
            }
        }
    }
    //endregion

    //region App permissions settings screen

    private static void findDifferenceBetweenPermissionNameToSwitchStatusAndSendEvents(AccessibilityNodeInfo source) {
        if (source == null) {
            return;
        }

        /**
         * NOTE: Record the permission settings for later permission deny warning use.
         */
        List<AccessibilityNodeInfo> permissionRecyclerViews = source.findAccessibilityNodeInfosByViewId("android:id/list");
        if (permissionRecyclerViews == null || permissionRecyclerViews.isEmpty()) {
            permissionRecyclerViews = source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/list");
        }
        if (permissionRecyclerViews != null && !permissionRecyclerViews.isEmpty()) {
            AccessibilityNodeInfo recyclerView = permissionRecyclerViews.get(0);
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = recyclerView.getChild(i);
                if (child == null || child.getPackageName() == null || child.getClassName() == null) {
                    continue;
                }
                /**
                 * This level is the LinearLayout for a permission name and its switch.
                 */
                AccessibilityNodeInfo permissionTitleNode = null;
                List<AccessibilityNodeInfo> permissionTitles = child.findAccessibilityNodeInfosByViewId("android:id/title");
                if (permissionTitles.isEmpty()) {
                    permissionTitles = child.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name");
                }
                if (permissionTitles.size() > 0) {
                    permissionTitleNode = permissionTitles.get(0);
                }

                AccessibilityNodeInfo switchTextNode = null;
                List<AccessibilityNodeInfo> switchTexts = child.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget");
                if (switchTexts.isEmpty()) {
                    switchTexts = child.findAccessibilityNodeInfosByViewId("android:id/switch_widget");
                    if (switchTexts.isEmpty()) {
                        switchTexts = child.findAccessibilityNodeInfosByViewId("android:id/switchWidget");
                    }
                }
                if (switchTexts.size() > 0) {
                    switchTextNode = switchTexts.get(0);
                }

                if (permissionTitleNode != null && permissionTitleNode.getText() != null
                        && switchTextNode != null && switchTextNode.getText() != null) {
                    String name = permissionTitleNode.getText().toString();
                    String status = switchTextNode.getText().toString();
                    if (permissionNames2permissionSwitchStatus.containsKey(name) &&
                            permissionNames2permissionSwitchStatus.get(name) != null &&
                            !Objects.equals(permissionNames2permissionSwitchStatus.get(name), status)) {
//                        Log.d(TAG, "Found difference in permission2status: " + name + ":" + status);
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
//            Log.d(TAG, "From App permissions screen, detected currently handled app name is " + currentlyHandledAppName);

            /**
             * Get app package name and version from app name.
             */
            currentlyHandledAppPackage = AppPackagesBroadcastReceiver.findPackageNameFromAppName(currentlyHandledAppName, packageManager);
            currentlyHandledAppVersion = AppPackagesBroadcastReceiver.getApplicationVersion(currentlyHandledAppPackage, packageManager);
//            Log.d(TAG, "From App permissions screen, detected currently handled app package name is " + currentlyHandledAppPackage);
//            Log.d(TAG, "From App permissions screen, detected currently handled app version is " + currentlyHandledAppVersion);
        }

        /**
         * NOTE: Record the permission settings for later permission deny warning use.
         */
        List<AccessibilityNodeInfo> permissionRecyclerViews = source.findAccessibilityNodeInfosByViewId("android:id/list");
        if (permissionRecyclerViews == null || permissionRecyclerViews.isEmpty()) {
            permissionRecyclerViews = source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/list");
        }
        if (permissionRecyclerViews != null && !permissionRecyclerViews.isEmpty()) {
            AccessibilityNodeInfo recyclerView = permissionRecyclerViews.get(0);
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = recyclerView.getChild(i);
                if (child == null || child.getPackageName() == null || child.getClassName() == null) {
                    continue;
                }
                /**
                 * This level is the LinearLayout for a permission name and its switch.
                 */
                AccessibilityNodeInfo permissionTitleNode = null;
                List<AccessibilityNodeInfo> permissionTitles = child.findAccessibilityNodeInfosByViewId("android:id/title");
                if (permissionTitles.isEmpty()) {
                    permissionTitles = child.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name");
                }
                if (permissionTitles.size() > 0) {
                    permissionTitleNode = permissionTitles.get(0);
                }

                AccessibilityNodeInfo switchTextNode = null;
                List<AccessibilityNodeInfo> switchTexts = child.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget");
                if (switchTexts.isEmpty()) {
                    switchTexts = child.findAccessibilityNodeInfosByViewId("android:id/switch_widget");
                    if (switchTexts.isEmpty()) {
                        switchTexts = child.findAccessibilityNodeInfosByViewId("android:id/switchWidget");
                    }
                }
                if (switchTexts.size() > 0) {
                    switchTextNode = switchTexts.get(0);
                }

                if (permissionTitleNode != null && permissionTitleNode.getText() != null
                        && switchTextNode != null && switchTextNode.getText() != null) {
                    permissionNames2permissionSwitchStatus.put(permissionTitleNode.getText().toString(), switchTextNode.getText().toString());
//                    Log.d(TAG, "In App permissions screen list, found permission name to switch status pair: "
//                            + permissionTitleNode.getText().toString() + " : "
//                            + switchTextNode.getText().toString());
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

        return (
                source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name").size() > 0 ||
                        // This changed in Android N
                        source.findAccessibilityNodeInfosByViewId("android:id/title").size() > 0
        ) && (
                source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget").size() > 0 ||
                        // This changed in Android N
                        source.findAccessibilityNodeInfosByViewId("android:id/switch_widget").size() > 0 ||
                        // This was a recent update for Nexus phones
                        source.findAccessibilityNodeInfosByViewId("android:id/switchWidget").size() > 0
        );
    }
    //endregion

    //region Send events to Firebase

    /**
     * Checks if required parameters for PermissionServerEvent are there and send event to Firebase.
     * Reset the rationale data after sending the permission event associated with it.
     *
     * @param initiatedByUser if the permission change is done by the user or initiated by the system
     */
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
                currentlyPermissionGranted, Boolean.toString(initiatedByUser), currentlyProactivePermissionRequestRationale,
                currentlyProactivePermissionRequestEventCorrelationId));

        currentlyHandledPermission = currentlyHandledSubsequentPermission;
        currentlyPermissionGranted = null;
        currentlyHandledSubsequentPermission = null;

        // TODO: reset rationale but if there are multiple permission requests asked after only one rationale, this would be wrong
        currentlyProactivePermissionRequestRationaleGranted = null;
        currentlyProactivePermissionRequestRationale = null;
        currentlyProactivePermissionRequestEventCorrelationId = null;
    }

    /**
     * Checks if required parameters for ProactivePermissionServerEvent are there and send the event to Firebase. Reset rationale data
     * after detecting user denied the proactive permission request.
     */
    private static void sendProactivePermissionRequestEventToFirebase() {
        if (currentlyHandledAppVersion == null || currentlyHandledAppVersion.isEmpty() ||
                currentlyHandledAppName == null || currentlyHandledAppName.isEmpty() ||
                currentlyProactivePermissionRequestRationaleGranted == null || currentlyProactivePermissionRequestRationaleGranted.isEmpty() ||
                currentlyProactivePermissionRequestRationale == null || currentlyProactivePermissionRequestRationale.isEmpty() ||
                currentlyHandledAppPackage == null || currentlyHandledAppPackage.isEmpty()) {
            return;
        }

        currentlyProactivePermissionRequestEventCorrelationId = UUID.randomUUID().toString();

        FirestoreProvider firestoreProvider = new FirestoreProvider();
        firestoreProvider.sendProactivePermissionEvent(ExperimentEventFactory.createProactivePermissionEvent(currentlyHandledAppName,
                currentlyHandledAppPackage, currentlyHandledAppVersion, currentlyProactivePermissionRequestRationale,
                currentlyProactivePermissionRequestRationaleGranted, currentlyProactivePermissionRequestEventCorrelationId));

        /**
         * If deny, then reset rationale, granted and event correlation id.
         */
        if (!Boolean.parseBoolean(currentlyProactivePermissionRequestRationaleGranted)) {
            currentlyProactivePermissionRequestRationaleGranted = null;
            currentlyProactivePermissionRequestRationale = null;
            currentlyProactivePermissionRequestEventCorrelationId = null;
        }
    }
    //endregion

    //region Runtime permission dialog

    /**
     * Extract the all name and permission name from a runtime permission dialog.
     *
     * @param event                    accessibility event
     * @param isFirstPermissionRequest if this is the first permission request in a series of requests
     */
    private static void extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(AccessibilityEvent event, boolean isFirstPermissionRequest) {
        for (CharSequence eventSubText : event.getText()) {
            Pattern permissionRegex = Pattern.compile(PrivaDroidApplication.getAppContext().getString(R.string.android_allow_x_to_x_screen_regex));
            Matcher permissionMatcher = permissionRegex.matcher(eventSubText);
            if (permissionMatcher.find()) {
                if (!isFirstPermissionRequest) {
//                    Log.d(TAG, "A consecutive second permission request dialog happens.");
                } else {
//                    Log.d(TAG, "A first permission request dialog happens.");
                }
                currentlyHandledAppName = permissionMatcher.group(1);
//                Log.d(TAG, "Extracted from runtime permission dialog currently handled app name is " + currentlyHandledAppName);

                String permissionText = permissionMatcher.group(2);
                if (isFirstPermissionRequest) {
                    currentlyHandledPermission = AccessibilityEventMonitorService.PERMISSION_DIALOG_STRINGS.get(permissionText);
//                    Log.d(TAG, "Extracted from runtime permission dialog currently handled permission is " + currentlyHandledPermission);
                } else {
                    currentlyHandledSubsequentPermission = AccessibilityEventMonitorService.PERMISSION_DIALOG_STRINGS.get(permissionText);
//                    Log.d(TAG, "Extracted from runtime permission dialog currently handled subsequent permission is " + currentlyHandledSubsequentPermission);
                }

                // check if app name belongs to package name
                if (currentlyHandledAppPackage != null && currentlyHandledAppName != null &&
                        !currentlyHandledAppName.equals(getApplicationNameFromPackageName(currentlyHandledAppPackage, packageManager))) {
                    // NOTE: change to better algo, currently compare app name to every package app name and find the right package name
                    currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, packageManager);
//                    Log.d(TAG, "Used app name to find app package name is " + currentlyHandledAppPackage);
                }

                currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, packageManager);
//                Log.d(TAG, "Extracted from runtime permission dialog currently handled app version is " + currentlyHandledAppVersion);
                break;
            }
        }
    }

    /**
     * Extract permission and app name from runtime permission request dialog.
     */
    private static void extractInformationFromPermissionDialog(AccessibilityEvent event) {
        /**
         * Find the last active app package
         */
        currentlyHandledAppPackage = RuntimePermissionAppUtil.getLastActiveAppPackageName();
        if (currentlyHandledAppPackage == null || currentlyHandledAppPackage.isEmpty()) {
            return;
        }
//        Log.d(TAG, "Extract currently handled app package name from runtime permission request dialog: " + currentlyHandledAppPackage);

        /**
         * Extract permission name and app name from dialog text
         */
        extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(event, true);
    }

    /**
     * Extract Allow/Deny decision from runtime permission request dialog.
     *
     * @param source source node of the action event
     */
    private static void processPermissionDialogAction(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null) {
            return;
        }

        /**
         * Extract action option, Allow or Deny.
         */
        String actionTextLower = source.getText().toString().toLowerCase();
        if (actionTextLower.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_screen_text).toLowerCase())) {
            currentlyPermissionGranted = Boolean.toString(true);
//            Log.d(TAG, "Detected grant in runtime permission dialog.");
        } else if (actionTextLower.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text).toLowerCase())) {
            currentlyPermissionGranted = Boolean.toString(false);
//            Log.d(TAG, "Detected deny in runtime permission dialog.");
        }
    }

    /**
     * Check if it's an action (deny/allow) in a runtime permission request dialog.
     *
     * @param source source node of the event
     * @return true if user clicks Allow or Deny
     */
    private static boolean isPermissionsDialogAction(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null || source.getPackageName() == null) {
            return false;
        }

        String packageName = source.getPackageName().toString();
        if (!(packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE) || packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE))) {
            return false;
        }

        String nodeTextLowercase = source.getText().toString().toLowerCase();
        return source.getClassName().equals(BUTTON_CLASS_NAME) &&
                (nodeTextLowercase.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_screen_text).toLowerCase()) ||
                        nodeTextLowercase.equals(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text).toLowerCase()));
    }

    /**
     * Process the subsequent runtime permission requests after the first one in a series of
     * consecutive permission requests.
     *
     * @param event accessibility event
     */
    private static void processConsecutivePermissionRequestByAnApp(AccessibilityEvent event) {
        List<CharSequence> eventText = event.getText();
        if (eventText == null || eventText.isEmpty()) {
            return;
        }

        extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(event, false);
    }

    /**
     * Checks if the system is showing a runtime permission request dialog, spawned by an app to ask
     * for a runtime permission.
     *
     * @param source source node of the event
     * @return true if it is a runtime permission dialog
     */
    private static boolean isPermissionsDialog(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        List<AccessibilityNodeInfo> permissionDenyButton = source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button");
        return permissionDenyButton != null && permissionDenyButton.size() > 0;
    }
    //endregion
}
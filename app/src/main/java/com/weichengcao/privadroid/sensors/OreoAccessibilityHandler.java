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

public class OreoAccessibilityHandler {

    private final static String TAG = MarshmallowAccessibilityHandler.class.getSimpleName();

    private final static PackageManager packageManager = PrivaDroidApplication.getAppContext().getPackageManager();

    /**
     * Runtime permission dialog texts
     */
    private static final String ALLOW_KEYWORD = "allow";
    private static final String DENY_KEYWORD = "deny";

    private static String currentlyHandledAppPackage = null;
    private static String currentlyHandledAppName = null;
    private static String currentlyHandledPermission = null;
    private static String currentlyHandledSubsequentPermission = null;
    private static String currentlyHandledAppVersion = null;
    private static String currentlyInitiatedByUser = null;
    private static String currentlyPermissionGranted = null;

    /**
     * If we are inside the screen where all permission settings are shown for a single app.
     */
    private static boolean insideSettingsAppPermissionsForAnAppScreen = false;

    /**
     * If we are inside the "Apps & notifications" screen.
     */
    private static boolean insideSettingsAppAndNotificationsScreen = false;

    /**
     * If we are inside the screen where all apps are listed and each app directs to the screen
     * where app details are shown.
     */
    private static boolean insideSettingsAppListScreenOrChildren = false;

    /**
     * This screen is the screen where all permissions are listed and each permission directs to
     * the screen where all the apps requiring that permission are shown.
     */
    private static boolean insideSettingsPermissionListScreen = false;

    /**
     * This screen represents a single permission and all the apps that ask for this permission and
     * their switch status.
     */
    private static boolean insideSinglePermissionToAllAppsSettingsScreen = false;

    private static boolean runIntoPermissionDenyWarning = false;
    private static HashMap<String, String> permissionNames2permissionSwitchStatus = new HashMap<>();

    public static void processAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo source = event.getSource();

        switch (eventType) {
            // TODO: directly detecting the app permission and single permission to all apps screen to avoid relying on the sequence of operations
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (isPermissionsDialog(source)) {
                    Log.d(TAG, "We are in a runtime permission dialog.");
                    extractInformationFromPermissionDialog(event);
                } else if (insideSettingsAppListScreenOrChildren && isSettingsAppPermissionsScreen(source)) {
                    Log.d(TAG, "We are in the App permissions screen.");
                    insideSettingsAppPermissionsForAnAppScreen = true;
                    runIntoPermissionDenyWarning = false;
                    permissionNames2permissionSwitchStatus = new HashMap<>();

                    extractAppNameFromSettingsAppPermissionsScreenAndRecordCurrentPermissionSettings(source);
                } else if (isPermissionDenyWarningDialog(source)) {
                    Log.d(TAG, "We ran in to a permission deny warning dialog in App permissions screen.");
                    runIntoPermissionDenyWarning = true;
                } else if (isAppsAndNotificationsScreen(source)) {
                    Log.d(TAG, "We are in the Settings -> Apps & notifications screen.");
                    insideSettingsAppAndNotificationsScreen = true;
                    insideSettingsAppListScreenOrChildren = false;
                    insideSettingsAppPermissionsForAnAppScreen = false;
                    insideSettingsPermissionListScreen = false;
                    insideSinglePermissionToAllAppsSettingsScreen = false;

                    currentlyHandledAppName = null;
                    currentlyHandledAppPackage = null;
                    currentlyHandledAppVersion = null;
                    currentlyHandledPermission = null;
                    currentlyHandledSubsequentPermission = null;
                    currentlyInitiatedByUser = null;
                    currentlyPermissionGranted = null;
                }
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                /**
                 * NOTE: A consecutive second permission request dialog happens.
                 */
                processConsecutivePermissionRequestByAnApp(event);
                Log.d(TAG, "A consecutive second permission request dialog happens.");
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (!runIntoPermissionDenyWarning && isPermissionsDialogAction(source)) {
                    Log.d(TAG, "We acted in a runtime permission request dialog.");
                    processPermissionDialogAction(source);

                    sendPermissionEventToFirebase(false);
                } else if (isTogglingPermissionInAppPermissionsScreen(source)) {
                    Log.d(TAG, "We toggled a switch in App permissions screen.");
                    /**
                     * Only send the permission event to server if not encountering permission deny warning dialog.
                     * NOTE: Detection of permission deny warning happens after detection of click, causing incorrect permission grant event. Can log the permission settings when inside App permissions screen?
                     */
                    if (!runIntoPermissionDenyWarning && !ifClickedPermissionDidNotChangeDueToDenyAlert()) {
                        Log.d(TAG, "No permission deny warning dialog popped up. We effectively toggled switch.");

                        // Update permission name to switch status map
                        permissionNames2permissionSwitchStatus.put(currentlyHandledPermission, Boolean.parseBoolean(currentlyPermissionGranted) ?
                                PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text) :
                                PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_off_screen_text));

                        sendPermissionEventToFirebase(true);
                    }
                } else if (runIntoPermissionDenyWarning && isDenyingInPermissionDenyWarningDialog(source)) {
                    Log.d(TAG, "We still denied the permission in the permission deny warning dialog.");
                    runIntoPermissionDenyWarning = false;

                    // Update permission name to switch status map
                    permissionNames2permissionSwitchStatus.put(currentlyHandledPermission, Boolean.parseBoolean(currentlyPermissionGranted) ?
                            PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text) :
                            PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_off_screen_text));

                    sendPermissionEventToFirebase(true);
                } else if (insideSettingsAppAndNotificationsScreen && isClickingIntoAppInfoOption(source)) {
                    Log.d(TAG, "Detected a click on App info in Apps & notifications.");
                    insideSettingsAppAndNotificationsScreen = false;
                    insideSettingsPermissionListScreen = false;
                    insideSettingsAppListScreenOrChildren = true;
                } else if (insideSettingsAppAndNotificationsScreen && isClickingIntoAppPermissionsOption(source)) {
                    Log.d(TAG, "Detected a click on App permissions in Apps & notifications.");
                    insideSettingsAppAndNotificationsScreen = false;
                    insideSettingsPermissionListScreen = true;
                    insideSettingsAppListScreenOrChildren = false;
                } else if (insideSettingsPermissionListScreen && isClickingIntoAPermissionWithListOfApps(source)) {
                    Log.d(TAG, "Detected a click on a permission in App permissions screen containing a list of apps.");
                    insideSinglePermissionToAllAppsSettingsScreen = true;
                    insideSettingsPermissionListScreen = false;
                } else if (insideSinglePermissionToAllAppsSettingsScreen && isTogglingAccessSwitchInSinglePermissionToAllAppsScreen(source)) {
                    Log.d(TAG, "Detected a toggle on a app permission in e.g. Body Sensors permissions.");
                    sendPermissionEventToFirebase(true);
                }
                break;
        }
    }

    /**
     * Detect if user toggles a permission access switch for an app in permission to all apps screen.
     */
    private static boolean isTogglingAccessSwitchInSinglePermissionToAllAppsScreen(AccessibilityNodeInfo source) {
        if (source == null) {
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
                    && className.equals("android.widget.TextView")) {
                currentlyHandledAppName = child.getText().toString();
                currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, PrivaDroidApplication.getAppContext().getPackageManager());
                currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, PrivaDroidApplication.getAppContext().getPackageManager());
            } else if ((packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE)
                    || packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE))
                    && className.equals("android.widget.Switch")) {
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
     * Detect if a click is on e.g. "Body Sensors" in "App permissions" screen containing a list of apps.
     */
    private static boolean isClickingIntoAPermissionWithListOfApps(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        String[] permissionCategories = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.android_permission_categories);

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
                    if (className.toLowerCase().equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME.toLowerCase())) {
                        /**
                         * Check if it's a permission.
                         */
                        for (String p : permissionCategories) {
                            if (text.toLowerCase().equals(p.toLowerCase())) {
                                currentlyHandledPermission = p;
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Detect if a click is on "App permissions" in "Apps & notifications" screen.
     */
    private static boolean isClickingIntoAppPermissionsOption(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

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
                    if (className.toLowerCase().equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME.toLowerCase()) &&
                            text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.android_o_app_permissions_screen_text).toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Detect if a click is on "App info" in "Apps & notifications" screen.
     */
    private static boolean isClickingIntoAppInfoOption(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

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
                    if (className.toLowerCase().equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME.toLowerCase()) &&
                            text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.android_o_app_info_screen_text).toLowerCase())) {
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

        boolean foundAppInfoOptions = false;
        boolean foundAppPermissionsOptions = false;

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
                    if (className.toLowerCase().equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME.toLowerCase()) &&
                            text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.android_o_app_info_screen_text).toLowerCase())) {
                        foundAppInfoOptions = true;
                    } else if (className.toLowerCase().equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME.toLowerCase()) &&
                            text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.android_o_app_permissions_screen_text).toLowerCase())) {
                        foundAppPermissionsOptions = true;
                    }
                }
            }
        }

        return foundAppInfoOptions && foundAppPermissionsOptions;
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
                    currentlyHandledSubsequentPermission = AccessibilityEventMonitorService.PERMISSION_DIALOG_STRINGS.get(permissionText);
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

    private static void processConsecutivePermissionRequestByAnApp(AccessibilityEvent event) {
        List<CharSequence> eventText = event.getText();
        if (eventText == null || eventText.isEmpty()) {
            return;
        }

        extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(event, false);
    }

    private static boolean ifClickedPermissionDidNotChangeDueToDenyAlert() {
        if (permissionNames2permissionSwitchStatus == null || !permissionNames2permissionSwitchStatus.containsKey(currentlyHandledPermission)) {
            return false;
        }

        return Boolean.parseBoolean(currentlyPermissionGranted) && permissionNames2permissionSwitchStatus.get(currentlyHandledPermission)
                .equals(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text));
    }

    /**
     * Detecting if user is denying the permission deny warning dialog.
     */
    private static boolean isDenyingInPermissionDenyWarningDialog(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        if (source.getPackageName() != null && source.getClassName() != null && source.getText() != null) {
            String packageName = source.getPackageName().toString();
            String className = source.getClassName().toString();
            if ((packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE)
                    || packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE))
                    && className.equals(AndroidSdkConstants.BUTTON_CLASS_NAME)) {
                String text = source.getText().toString();
                if (text.toLowerCase().equals(PrivaDroidApplication.getAppContext().getString(R.string.android_n_deny_anyway_in_permission_deny_warning_dialog_screen_text).toLowerCase())) {
                    currentlyPermissionGranted = Boolean.toString(false);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Detecting if the permission deny warning dialog ("basic features disabled") shows up.
     * Don't trigger the subsequently detected toggling permission switch because it's always ON and
     * we should wait for users' decision on the warning dialog.
     */
    private static boolean isPermissionDenyWarningDialog(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        List<AccessibilityNodeInfo> warningMessageNodes = source.findAccessibilityNodeInfosByViewId("android:id/message");
        if (warningMessageNodes != null && warningMessageNodes.size() == 1) {
            AccessibilityNodeInfo warningMessage = warningMessageNodes.get(0);
            String warning = warningMessage.getText().toString();
            if (warning.toLowerCase().contains(PrivaDroidApplication.getAppContext().getString(R.string.basic_feature_in_message_screen_text).toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Detecting if user is effectively toggling permission toggle inside Settings -> Apps -> App info -> App permissions.
     */
    private static boolean isTogglingPermissionInAppPermissionsScreen(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppPermissionsForAnAppScreen) {
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

        if (insideSinglePermissionToAllAppsSettingsScreen) {
            return;
        }

        currentlyHandledPermission = currentlyHandledSubsequentPermission;
        currentlyHandledSubsequentPermission = null;
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

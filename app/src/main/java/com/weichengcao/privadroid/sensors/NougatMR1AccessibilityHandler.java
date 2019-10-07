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

import static com.weichengcao.privadroid.sensors.AccessibilityEventMonitorService.PROACTIVE_PERMISSION_REQUEST_DIALOG_VIEW_THRESHOLD;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.findPackageNameFromAppName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationNameFromPackageName;
import static com.weichengcao.privadroid.sensors.AppPackagesBroadcastReceiver.getApplicationVersion;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_SHORTHAND;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.TEXTVIEW_SHORTHAND;

class NougatMR1AccessibilityHandler {

    private final static String TAG = NougatMR1AccessibilityHandler.class.getSimpleName();

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
    private static String currentlyProactivePermissionRequestPackage = null;

    private static boolean insideSettingsAppPermissionsScreen = false;

    private static boolean runIntoPermissionDenyWarning = false;
    private static boolean runIntoAppProactivePermissionRequestDialog = false;
    private static HashMap<String, String> permissionNames2permissionSwitchStatus = new HashMap<>();

    static void processAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo source = event.getSource();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (isPermissionsDialog(source)) {
//                    Log.d(TAG, "We are in a runtime permission dialog.");
                    extractInformationFromPermissionDialog(event);
                } else if (isSettingsAppList(source)) {
//                    Log.d(TAG, "We are in the Settings -> Apps screen.");
                    runIntoPermissionDenyWarning = false;

                    currentlyHandledAppName = null;
                    currentlyHandledAppPackage = null;
                    currentlyHandledAppVersion = null;
                    currentlyHandledPermission = null;
                    currentlyHandledSubsequentPermission = null;
                    currentlyPermissionGranted = null;
                } else if (isSettingsAppPermissionsScreen(source)) {
//                    Log.d(TAG, "We are in the App permissions screen.");
                    insideSettingsAppPermissionsScreen = true;
                    runIntoPermissionDenyWarning = false;

                    extractAppNameFromSettingsAppPermissionsScreenAndRecordCurrentPermissionSettings(source);
                } else if (isPermissionDenyWarningDialog(source)) {
//                    Log.d(TAG, "We ran in to a permission deny warning dialog in App permissions screen.");
                    runIntoPermissionDenyWarning = true;
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
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (isPermissionsDialogAction(source)) {
//                    Log.d(TAG, "We acted in a runtime permission request dialog.");
                    processPermissionDialogAction(source);

                    sendPermissionEventToFirebase(false);
                } else if (isTogglingPermissionInAppPermissionsScreen(source)) {
//                    Log.d(TAG, "We toggled a switch in App permissions screen.");
                    /**
                     * Only send the permission event to server if not encountering permission deny warning dialog.
                     * NOTE: Detection of permission deny warning happens after detection of click, causing incorrect permission grant event. Can log the permission settings when inside App permissions screen?
                     */
                    if (!runIntoPermissionDenyWarning && !ifClickedPermissionDidNotChangeDueToDenyAlert()) {
//                        Log.d(TAG, "No permission deny warning dialog popped up. We effectively toggled switch.");

                        // Update permission name to switch status map
                        permissionNames2permissionSwitchStatus.put(currentlyHandledPermission, Boolean.parseBoolean(currentlyPermissionGranted) ?
                                PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text) :
                                PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_off_screen_text));

                        sendPermissionEventToFirebase(true);
                    }
                } else if (isDenyingInPermissionDenyWarningDialog(source)) {
//                    Log.d(TAG, "We still denied the permission in the permission deny warning dialog.");
                    runIntoPermissionDenyWarning = false;

                    // Update permission name to switch status map
                    permissionNames2permissionSwitchStatus.put(currentlyHandledPermission, Boolean.parseBoolean(currentlyPermissionGranted) ?
                            PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text) :
                            PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_off_screen_text));

                    sendPermissionEventToFirebase(true);
                } else if (isClickingInProactivePermissionRequestDialog(source)) {
//                    Log.d(TAG, "Detected a click in proactive permission request dialog.");
                    processProactivePermissionRequestDialogAction(source);

                    sendProactivePermissionRequestEventToFirebase();
                } else {
//                    Log.d(TAG, "Unhandled TYPE_VIEW_CLICKED event");
                }
                break;
        }
        if (source != null) {
            source.recycle();
        }
    }

    //region Proactive permission requests

    /**
     * Process and extract the decision of proactive permission request.
     */
    private static void processProactivePermissionRequestDialogAction(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null || !source.getClassName().equals(BUTTON_CLASS_NAME)) {
            return;
        }

        String nodeTextLowercase = source.getText().toString().toLowerCase();
        HashSet<String> proactivePermissionGrantButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_grant_button_texts)));
        HashSet<String> proactivePermissionDenyButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_deny_button_texts)));

        if (proactivePermissionGrantButtonTexts.contains(nodeTextLowercase)) {
            currentlyProactivePermissionRequestRationaleGranted = Boolean.toString(true);
//            Log.d(TAG, "Detected user click grant related button in proactive permission request dialog.");
        } else if (proactivePermissionDenyButtonTexts.contains(nodeTextLowercase)) {
            currentlyProactivePermissionRequestRationaleGranted = Boolean.toString(false);
//            Log.d(TAG, "Detected user click deny related button in proactive permission request dialog.");
        }
    }

    /**
     * Detect if a click happens in proactive permission request dialog.
     */
    private static boolean isClickingInProactivePermissionRequestDialog(AccessibilityNodeInfo source) {
        if (source == null || source.getText() == null || !runIntoAppProactivePermissionRequestDialog) {
            return false;
        }

        HashSet<String> proactivePermissionGrantButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_grant_button_texts)));
        HashSet<String> proactivePermissionDenyButtonTexts = new HashSet<>(Arrays.asList(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.proactive_permission_request_dialog_deny_button_texts)));

        String nodeTextLowercase = source.getText().toString().toLowerCase();
        return source.getClassName().equals(BUTTON_CLASS_NAME) &&
                (proactivePermissionGrantButtonTexts.contains(nodeTextLowercase) ||
                        proactivePermissionDenyButtonTexts.contains(nodeTextLowercase));
    }

    /**
     * Extract rationale message from proactive permission request.
     */
    private static void extractRationaleMessageFromProactivePermissionRequest(AccessibilityNodeInfo source) {
        if (source == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        int textViewCount = 0;
        int buttonViewCount = 0;

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
                        textViewCount++;
                    } else if (cur.getClassName().toString().equals(BUTTON_CLASS_NAME)) {
                        sb.append(BUTTON_SHORTHAND).append(": ")
                                .append(cur.getText().toString()).append(". ");
                        buttonViewCount++;
                    }
                }
            }
        }

        if (textViewCount >= PROACTIVE_PERMISSION_REQUEST_DIALOG_VIEW_THRESHOLD ||
                buttonViewCount >= PROACTIVE_PERMISSION_REQUEST_DIALOG_VIEW_THRESHOLD) {
            return;
        }

        currentlyProactivePermissionRequestRationale = sb.toString();
//        Log.d(TAG, "Extracted proactive permission request rationale to be " + currentlyProactivePermissionRequestRationale);

        /**
         * Find the last active app package
         */
        if (source.getPackageName() != null) {
            currentlyProactivePermissionRequestPackage = source.getPackageName().toString();
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
        boolean foundPermissionActionKeywords = false;
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
                            break;
                        }
                    }
                    for (String s : AccessibilityEventMonitorService.PERMISSION_ACTION_RELATED_KEYWORDS) {
                        if (textLowerCase.contains(s)) {
                            foundPermissionActionKeywords = true;
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
                    }
                }
            }
        }

        return foundButtons && foundRationaleKeywords && foundPermissionActionKeywords;
    }
    //endregion

    //region Permission warning dialog
    private static boolean ifClickedPermissionDidNotChangeDueToDenyAlert() {
        if (permissionNames2permissionSwitchStatus == null ||
                !permissionNames2permissionSwitchStatus.containsKey(currentlyHandledPermission) ||
                permissionNames2permissionSwitchStatus.get(currentlyHandledPermission) == null) {
            return false;
        }

        return Boolean.parseBoolean(currentlyPermissionGranted) && Objects.equals(permissionNames2permissionSwitchStatus.get(currentlyHandledPermission), PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text));
    }

    /**
     * Detecting if user is denying the permission deny warning dialog.
     */
    private static boolean isDenyingInPermissionDenyWarningDialog(AccessibilityNodeInfo source) {
        if (source == null || !runIntoPermissionDenyWarning) {
            return false;
        }

        if (source.getPackageName() != null && source.getClassName() != null && source.getText() != null) {
            String packageName = source.getPackageName().toString();
            String className = source.getClassName().toString();
            if ((packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE)
                    || packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE))
                    && className.equals(AndroidSdkConstants.BUTTON_CLASS_NAME)) {
                String text = source.getText().toString();
                if (text.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_n_deny_anyway_in_permission_deny_warning_dialog_screen_text))) {
                    currentlyPermissionGranted = Boolean.toString(false);
//                    Log.d(TAG, "User clicked deny anyway in permission warning dialog.");
                    return true;
                }
            }
        }

//        Log.d(TAG, "User did not click deny anyway in permission warning dialog.");
        return false;
    }

    /**
     * Detecting if the permission deny warning dialog ("basic features disabled") shows up.
     * Don't trigger the subsequently detected toggling permission switch because it's always ON and
     * we should wait for users' decision on the warning dialog.
     */
    private static boolean isPermissionDenyWarningDialog(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppPermissionsScreen) {
            return false;
        }

        List<AccessibilityNodeInfo> warningMessageNodes = source.findAccessibilityNodeInfosByViewId("android:id/message");
        if (warningMessageNodes != null && warningMessageNodes.size() == 1) {
            AccessibilityNodeInfo warningMessage = warningMessageNodes.get(0);
            if (warningMessage == null || warningMessage.getText() == null) {
                return false;
            }
            String warning = warningMessage.getText().toString();
            return warning.toLowerCase().contains(PrivaDroidApplication.getAppContext().getString(R.string.android_basic_feature_in_message_screen_text).toLowerCase());
        }

        return false;
    }
    //endregion

    //region App permissions list screen

    /**
     * Detecting if user is effectively toggling permission toggle inside Settings -> Apps -> App info -> App permissions.
     */
    private static boolean isTogglingPermissionInAppPermissionsScreen(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppPermissionsScreen || runIntoPermissionDenyWarning) {
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

                if (cur.getPackageName() == null || cur.getText() == null || cur.getClassName() == null) {
                    continue;
                }
                String packageName = cur.getPackageName().toString();
                String className = cur.getClassName().toString();

                if ((packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE)
                        || packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE))
                        && className.equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME)) {
                    currentlyHandledPermission = cur.getText().toString();
                } else if ((packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE)
                        || packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE))
                        && className.equals(AndroidSdkConstants.SWITCH_CLASS_NAME)) {
                    String switchStatus = cur.getText().toString();
                    if (switchStatus.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_on_screen_text))) {
                        currentlyPermissionGranted = Boolean.toString(true);
                    } else if (switchStatus.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.permission_switch_status_off_screen_text))) {
                        currentlyPermissionGranted = Boolean.toString(false);
                    }
                }

                /**
                 * If we got both permission name and grant status.
                 */
                if (currentlyHandledPermission != null && currentlyPermissionGranted != null) {
//                    Log.d(TAG, "Toggling permission " + currentlyHandledPermission + " to be " + currentlyPermissionGranted + " in app permissions list.");
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Extract app name from Settings -> Apps -> App info -> App permissions screen and record the
     * current permission settings.
     */
    private static void extractAppNameFromSettingsAppPermissionsScreenAndRecordCurrentPermissionSettings(AccessibilityNodeInfo source) {
        if (source == null || !insideSettingsAppPermissionsScreen) {
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

    //region App list in Settings

    /**
     * Detecting if we arrive at the "Apps" screen containing a list of all apps.
     */
    private static boolean isSettingsAppList(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        /**
         * com.android.settings:id/content_parent LinearLayout holds "Apps" and list of app names.
         */
        List<AccessibilityNodeInfo> appsLists = source.findAccessibilityNodeInfosByViewId("com.android.settings:id/content_parent");
        if (appsLists != null && !appsLists.isEmpty()) {
            AccessibilityNodeInfo appsScreenLinearLayout = appsLists.get(0);

            Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
            allChildren.add(appsScreenLinearLayout);

            while (!allChildren.isEmpty()) {
                AccessibilityNodeInfo cur = allChildren.poll();
                /**
                 * Add its children.
                 */
                if (cur != null) {
                    int childCount = cur.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        allChildren.add(cur.getChild(i));
                    }

                    /**
                     * Check current node.
                     */
                    if (cur.getText() != null && cur.getClassName() != null) {
                        String text = cur.getText().toString();
                        String className = cur.getClassName().toString();
                        if (className.equals(AndroidSdkConstants.TEXTVIEW_CLASS_NAME) &&
                                text.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.apps_screen_text))) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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

        /**
         * Don't add the proactive permission dialog if the current permission request package does
         * not match the package where we detected proactive permission request with
         */
        FirestoreProvider firestoreProvider = new FirestoreProvider();
        if (currentlyProactivePermissionRequestPackage != null && currentlyProactivePermissionRequestPackage.equalsIgnoreCase(currentlyHandledAppPackage)) {
            firestoreProvider.sendPermissionEvent(ExperimentEventFactory.createPermissionEvent(currentlyHandledAppName,
                    currentlyHandledAppPackage, currentlyHandledAppVersion, currentlyHandledPermission,
                    currentlyPermissionGranted, Boolean.toString(initiatedByUser), currentlyProactivePermissionRequestRationale,
                    currentlyProactivePermissionRequestEventCorrelationId), true);
        } else {
            firestoreProvider.sendPermissionEvent(ExperimentEventFactory.createPermissionEvent(currentlyHandledAppName,
                    currentlyHandledAppPackage, currentlyHandledAppVersion, currentlyHandledPermission,
                    currentlyPermissionGranted, Boolean.toString(initiatedByUser), null, null), true);
        }

        currentlyHandledPermission = currentlyHandledSubsequentPermission;
        currentlyPermissionGranted = null;
        currentlyHandledSubsequentPermission = null;

        // TODO: reset rationale but if there are multiple permission requests asked after only one rationale, this would be wrong
        currentlyProactivePermissionRequestRationaleGranted = null;
        currentlyProactivePermissionRequestRationale = null;
        currentlyProactivePermissionRequestEventCorrelationId = null;
        currentlyProactivePermissionRequestPackage = null;
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
            currentlyProactivePermissionRequestPackage = null;
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
            if (eventSubText == null || eventSubText.length() == 0) {
                continue;
            }

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
        String actionText = source.getText().toString();
        if (actionText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_screen_text))) {
            currentlyPermissionGranted = Boolean.toString(true);
//            Log.d(TAG, "Detected grant in runtime permission dialog.");
        } else if (actionText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text))) {
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
        if (source == null || source.getText() == null || source.getPackageName() == null || runIntoPermissionDenyWarning) {
            return false;
        }

        String packageName = source.getPackageName().toString();
        if (!(packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE) || packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE))) {
            return false;
        }

        String nodeText = source.getText().toString();
        return source.getClassName().equals(BUTTON_CLASS_NAME) &&
                (nodeText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_screen_text)) ||
                        nodeText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text)));
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

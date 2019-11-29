package com.weichengcao.privadroid.sensors;

import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.AndroidSdkConstants;
import com.weichengcao.privadroid.database.ExperimentEventFactory;
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
import static com.weichengcao.privadroid.sensors.PermissionDialogReadTimeHandler.NANOSECOND_TO_SECOND;
import static com.weichengcao.privadroid.sensors.PermissionDialogReadTimeHandler.permissionDialogFirstOpenTime;
import static com.weichengcao.privadroid.sensors.PermissionDialogReadTimeHandler.permissionDialogReadTimeInSeconds;
import static com.weichengcao.privadroid.sensors.PreviousScreenTextHandler.currentlyPreviousScreenContextText;
import static com.weichengcao.privadroid.sensors.PreviousScreenTextHandler.processPreviousDialogText;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.BUTTON_SHORTHAND;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.TEXTVIEW_CLASS_NAME;
import static com.weichengcao.privadroid.util.AndroidSdkConstants.TEXTVIEW_SHORTHAND;

public class QAccessibilityHandler {

    private final static String TAG = QAccessibilityHandler.class.getSimpleName();

    private final static PackageManager packageManager = PrivaDroidApplication.getAppContext().getPackageManager();

    public static final String FOREGROUND_ONLY = "allow_foreground_only";

    private static String currentlyHandledAppPackage = null;
    private static String currentlyHandledAppName = null;
    private static String currentlyHandledPermission = null;
    private static String currentlyHandledAppVersion = null;
    private static String currentlyPermissionGranted = null;
    private static String currentlyProactivePermissionRequestRationale = null;
    private static String currentlyProactivePermissionRequestRationaleGranted = null;
    private static String currentlyProactivePermissionRequestEventCorrelationId = null;
    private static String currentlyProactivePermissionRequestPackage = null;

    private static boolean runIntoAppProactivePermissionRequestDialog = false;

    private static HashMap<String, String> singlePermissionSettingForAnApp = new HashMap<>();
    private static boolean insideSinglePermissionSettingForAnAppScreen = false;

    static void processAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo source = event.getSource();

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (isPermissionsDialog(source)) {
                    permissionDialogFirstOpenTime = System.nanoTime();
                    extractInformationFromPermissionDialog(event);
                } else if (isSettingsAppPermissionsScreen(source)) {
                    singlePermissionSettingForAnApp = new HashMap<>();
                    insideSinglePermissionSettingForAnAppScreen = false;

                    currentlyHandledAppName = null;
                    currentlyHandledAppPackage = null;
                    currentlyHandledAppVersion = null;
                    currentlyHandledPermission = null;
                    currentlyPermissionGranted = null;
                } else if (isSinglePermissionSettingForAnAppAndExtractPermissionAndAppName(source)) {
                    insideSinglePermissionSettingForAnAppScreen = true;
                    if (!singlePermissionSettingForAnApp.isEmpty()) {
                        findDifferenceBetweenSinglePermissionSettingForAnApp(source);
                    }
                    extractSinglePermissionSettingAndAppNameForAnApp(source);
                } else if (isAppProactivePermissionRequest(source)) {
                    runIntoAppProactivePermissionRequestDialog = true;

                    extractRationaleMessageFromProactivePermissionRequest(source);
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (insideSinglePermissionSettingForAnAppScreen) {
                    if (!singlePermissionSettingForAnApp.isEmpty()) {
                        findDifferenceBetweenSinglePermissionSettingForAnApp(source);
                    }
                    extractSinglePermissionSettingAndAppNameForAnApp(source);
                } else if (isAppProactivePermissionRequest(source)) {
                    runIntoAppProactivePermissionRequestDialog = true;

                    extractRationaleMessageFromProactivePermissionRequest(source);
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                if (isPermissionsDialogAction(source)) {
                    permissionDialogReadTimeInSeconds = (System.nanoTime() - permissionDialogFirstOpenTime) / NANOSECOND_TO_SECOND;

                    processPermissionDialogAction(source);
                    processPreviousDialogText(AccessibilityEventMonitorService.previousScreenTexts, currentlyHandledPermission);

                    sendPermissionEventToFirebase(false);
                } else if (isClickingInProactivePermissionRequestDialog(source, event)) {
                    processProactivePermissionRequestDialogAction(source, event);

                    sendProactivePermissionRequestEventToFirebase();
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
                && runIntoAppProactivePermissionRequestDialog && event.getText().get(0) != null) {
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

    //region Settings screen detection

    private static void findDifferenceBetweenSinglePermissionSettingForAnApp(AccessibilityNodeInfo source) {
        if (source == null) {
            return;
        }

        String permissionStatus = getPermissionSettingForSinglePermissionForAnApp(source);

        if (currentlyHandledPermission != null && singlePermissionSettingForAnApp.get(currentlyHandledPermission) != null) {
            if (!Objects.equals(singlePermissionSettingForAnApp.get(currentlyHandledPermission), permissionStatus)) {
                currentlyPermissionGranted = permissionStatus;
                sendPermissionEventToFirebase(true);
            }
        }
    }

    private static String getPermissionSettingForSinglePermissionForAnApp(AccessibilityNodeInfo source) {
        if (source == null) {
            return null;
        }

        List<AccessibilityNodeInfo> allowRadioButtons = source.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/allow_radio_button");
        List<AccessibilityNodeInfo> foregroundRadioButtons = source.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/foreground_only_radio_button");
        List<AccessibilityNodeInfo> denyRadioButtons = source.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/deny_radio_button");
        if (denyRadioButtons == null || denyRadioButtons.isEmpty()) {
            return null;
        }

        boolean hasAllow = false;
        boolean hasForegroundOnly = false;
        if (allowRadioButtons != null && !allowRadioButtons.isEmpty()) {
            hasAllow = true;
        }
        if (foregroundRadioButtons != null && !foregroundRadioButtons.isEmpty()) {
            hasForegroundOnly = true;
        }

        String permissionStatus = null;
        if (hasAllow) {
            AccessibilityNodeInfo allowRadio = allowRadioButtons.get(0);
            if (allowRadio.isChecked()) {
                permissionStatus = Boolean.toString(true);
            }
        }
        if (hasForegroundOnly) {
            AccessibilityNodeInfo foregroundRadio = foregroundRadioButtons.get(0);
            if (foregroundRadio.isChecked()) {
                permissionStatus = FOREGROUND_ONLY;
            }
        }
        AccessibilityNodeInfo denyRadio = denyRadioButtons.get(0);
        if (denyRadio.isChecked()) {
            permissionStatus = Boolean.toString(false);
        }

        return permissionStatus;
    }

    private static void extractSinglePermissionSettingAndAppNameForAnApp(AccessibilityNodeInfo source) {
        if (source == null) {
            return;
        }

        String permissionStatus = getPermissionSettingForSinglePermissionForAnApp(source);

        if (currentlyHandledPermission != null && currentlyHandledAppPackage != null && permissionStatus != null) {
            singlePermissionSettingForAnApp.put(currentlyHandledPermission, permissionStatus);
        }
    }

    private static boolean isSinglePermissionSettingForAnAppAndExtractPermissionAndAppName(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        boolean foundPermissionName = false;
        boolean foundAppName = false;

        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        while (!allChildren.isEmpty()) {
            AccessibilityNodeInfo cur = allChildren.poll();
            if (cur == null) {
                continue;
            }

            int childSize = cur.getChildCount();
            for (int i = 0; i < childSize; i++) {
                allChildren.add(cur.getChild(i));
            }

            if (cur.getClassName() == null || cur.getText() == null) {
                continue;
            }

            String className = cur.getClassName().toString();
            if (!className.equalsIgnoreCase(TEXTVIEW_CLASS_NAME)) {
                continue;
            }

            String text = cur.getText().toString();

            // match permission name
            Pattern permissionNamePattern = Pattern.compile(PrivaDroidApplication.getAppContext().getString(R.string.x_permission_access_for_this_app_regex));
            Matcher permissionMatcher = permissionNamePattern.matcher(text);
            if (permissionMatcher.find()) {
                currentlyHandledPermission = capitalizeFirstLetter(permissionMatcher.group(1)); // only capitalize the first letter
                foundPermissionName = true;
                break;
            }
        }

        // find app name
        List<AccessibilityNodeInfo> appNameNodes = source.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/entity_header_title");
        if (appNameNodes == null || appNameNodes.isEmpty()) {
            return false;
        }

        AccessibilityNodeInfo appNameNode = appNameNodes.get(0);
        if (appNameNode.getClassName() != null && appNameNode.getText() != null && appNameNode.getClassName().toString().equalsIgnoreCase(TEXTVIEW_CLASS_NAME)) {
            currentlyHandledAppName = appNameNode.getText().toString();
            currentlyHandledAppPackage = findPackageNameFromAppName(currentlyHandledAppName, packageManager);
            currentlyHandledAppVersion = getApplicationVersion(currentlyHandledAppPackage, packageManager);
            foundAppName = true;
        }

        return foundAppName && foundPermissionName;
    }

    /**
     * Detecting if we arrive at the "App permissions" screen containing permission settings of an app.
     */
    private static boolean isSettingsAppPermissionsScreen(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }

        boolean foundAllowed = false;
        boolean foundDenied = false;

        Queue<AccessibilityNodeInfo> allChildren = new LinkedList<>();
        allChildren.add(source);

        while (!allChildren.isEmpty()) {
            AccessibilityNodeInfo cur = allChildren.poll();
            if (cur == null) {
                continue;
            }

            int childSize = cur.getChildCount();
            for (int i = 0; i < childSize; i++) {
                allChildren.add(cur.getChild(i));
            }

            if (cur.getText() != null && cur.getClassName() != null) {
                String text = cur.getText().toString();
                String className = cur.getClassName().toString();

                if (className.equalsIgnoreCase(TEXTVIEW_CLASS_NAME)) {
                    if (text.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_settings_screen_allowed))) {
                        foundAllowed = true;
                    } else if (text.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_settings_screen_denied))) {
                        foundDenied = true;
                    }

                    if (foundAllowed && foundDenied) {
                        return true;
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

        long packageTotalForegroundTime = PermissionDialogReadTimeHandler.getTotalForegroundTime(currentlyHandledAppPackage);
        long packageRecentForegroundTime = PermissionDialogReadTimeHandler.getRecentForegroundTime(currentlyHandledAppPackage);
        long permissionDialogReadTime = initiatedByUser ? 0 : permissionDialogReadTimeInSeconds;

        /**
         * Don't add the proactive permission dialog if the current permission request package does
         * not match the package where we detected proactive permission request with
         */
        FirestoreProvider firestoreProvider = new FirestoreProvider();
        if (currentlyProactivePermissionRequestPackage != null && currentlyProactivePermissionRequestPackage.equalsIgnoreCase(currentlyHandledAppPackage)) {
            firestoreProvider.sendPermissionEvent(ExperimentEventFactory.createPermissionEvent(currentlyHandledAppName,
                    currentlyHandledAppPackage, currentlyHandledAppVersion, currentlyHandledPermission,
                    currentlyPermissionGranted, Boolean.toString(initiatedByUser), currentlyProactivePermissionRequestRationale,
                    currentlyProactivePermissionRequestEventCorrelationId, currentlyPreviousScreenContextText,
                    packageTotalForegroundTime, packageRecentForegroundTime, permissionDialogReadTime), true);
        } else {
            firestoreProvider.sendPermissionEvent(ExperimentEventFactory.createPermissionEvent(currentlyHandledAppName,
                    currentlyHandledAppPackage, currentlyHandledAppVersion, currentlyHandledPermission,
                    currentlyPermissionGranted, Boolean.toString(initiatedByUser), null, null,
                    currentlyPreviousScreenContextText, packageTotalForegroundTime, packageRecentForegroundTime,
                    permissionDialogReadTime), true);
        }

        currentlyPermissionGranted = null;

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
     * @param event accessibility event
     */
    private static void extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(AccessibilityEvent event) {
        for (CharSequence eventSubText : event.getText()) {
            if (eventSubText == null || eventSubText.length() == 0) {
                continue;
            }

            Pattern permissionRegex = Pattern.compile(PrivaDroidApplication.getAppContext().getString(R.string.android_allow_x_to_x_screen_regex));
            Matcher permissionMatcher = permissionRegex.matcher(eventSubText);
            if (permissionMatcher.find()) {
                currentlyHandledAppName = permissionMatcher.group(1);
//                Log.d(TAG, "Extracted from runtime permission dialog currently handled app name is " + currentlyHandledAppName);

                String permissionText = permissionMatcher.group(2);
                currentlyHandledPermission = AccessibilityEventMonitorService.PERMISSION_DIALOG_STRINGS.get(permissionText);
//                Log.d(TAG, "Extracted from runtime permission dialog currently handled permission is " + currentlyHandledPermission);

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
        extractPermissionNameAppNameFromRuntimePermissionRequestDialogText(event);
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
        } else if (actionText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text))) {
            currentlyPermissionGranted = Boolean.toString(false);
        } else if (actionText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_and_never_show_again))) {
            currentlyPermissionGranted = Boolean.toString(false);
        } else if (actionText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_only_foreground))) {
            currentlyPermissionGranted = FOREGROUND_ONLY;
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
        if (!(packageName.equals(AndroidSdkConstants.PACKAGE_INSTALLER_PACKAGE) ||
                packageName.equals(AndroidSdkConstants.GOOGLE_PACKAGE_INSTALLER_PACKAGE) ||
                packageName.equals(AndroidSdkConstants.GOOGLE_PERMISSION_CONTROLLER_PACKAGE) ||
                packageName.equals(AndroidSdkConstants.PERMISSION_CONTROLLER_PACKAGE))) {
            return false;
        }

        String nodeText = source.getText().toString();
        return source.getClassName() != null && source.getClassName().equals(BUTTON_CLASS_NAME) &&
                (nodeText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_only_foreground)) ||
                        nodeText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_allow_screen_text)) ||
                        nodeText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_screen_text)) ||
                        nodeText.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.android_dialog_deny_and_never_show_again)));
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

        List<AccessibilityNodeInfo> permissionDenyButton = source.findAccessibilityNodeInfosByViewId("com.android.permissioncontroller:id/permission_deny_button");
        return permissionDenyButton != null && permissionDenyButton.size() > 0;
    }
    //endregion

    private static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
    }
}

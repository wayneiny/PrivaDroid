package com.weichengcao.privadroid.sensors;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PreviousScreenTextHandler {

    private static final String NO_CONTEXT_MESSAGE_DETECTED = "NO_CONTEXT_MESSAGE_DETECTED";

    private static final String[] CAMERA_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.camera_set_values);
    private static final String[] CONTACTS_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.contacts_set_values);
    private static final String[] LOCATION_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.location_set_values);
    private static final String[] MICROPHONE_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.microphone_set_values);
    private static final String[] PHONE_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.phone_set_values);
    private static final String[] STORAGE_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.storage_set_values);
    private static final String[] BODY_SENSORS_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.body_sensors_set_values);
    private static final String[] CALENDAR_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.calendar_set_values);
    private static final String[] SMS_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.sms_set_values);
    private static final String[] CALL_LOGS_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.call_logs_set_values);
    private static final String[] SHARED_SET_VALUES = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.shared_set_values);

    private static final Map<String, HashSet<String>> CONTEXT_PERMISSION_RATIONALE_KEY_WORDS = new HashMap<>();

    static {
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_camera), new HashSet<>(Arrays.asList(CAMERA_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_contacts), new HashSet<>(Arrays.asList(CONTACTS_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_location), new HashSet<>(Arrays.asList(LOCATION_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_microphone), new HashSet<>(Arrays.asList(MICROPHONE_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_phone), new HashSet<>(Arrays.asList(PHONE_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_storage), new HashSet<>(Arrays.asList(STORAGE_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_body_sensors), new HashSet<>(Arrays.asList(BODY_SENSORS_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_calendar), new HashSet<>(Arrays.asList(CALENDAR_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_sms), new HashSet<>(Arrays.asList(SMS_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_call_logs), new HashSet<>(Arrays.asList(CALL_LOGS_SET_VALUES)));
        CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.put(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_shared), new HashSet<>(Arrays.asList(SHARED_SET_VALUES)));
    }

    static String currentlyPreviousScreenContextText = null;

    static void processPreviousDialogText(String[] previousScreenTexts, String currentlyHandlerPermissionName) {
        currentlyPreviousScreenContextText = "";
        if (previousScreenTexts == null || currentlyHandlerPermissionName == null || currentlyHandlerPermissionName.isEmpty()) {
            currentlyPreviousScreenContextText = NO_CONTEXT_MESSAGE_DETECTED;
            return;
        }

        boolean foundRelatedRationale = false;
        HashSet<String> contextPermissionRationaleKeywords = new HashSet<>();
        if (CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.containsKey(currentlyHandlerPermissionName)) {
            contextPermissionRationaleKeywords = CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.get(currentlyHandlerPermissionName);
        }

        int checkNumberOfPreviousScreens = 3;
        for (int i = 0; i < checkNumberOfPreviousScreens; i++) {

            String previousScreenText = previousScreenTexts[i];
            if (previousScreenText == null) {
                continue;
            }

            // try to match for default android permission message
            Pattern permissionRegex = Pattern.compile(PrivaDroidApplication.getAppContext().getString(R.string.previous_screen_default_permission_message_regex));
            Matcher permissionMatcher = permissionRegex.matcher(previousScreenText);

            if (permissionMatcher.find()) {
                String permissionText = permissionMatcher.group(2);
                String permissionString = AccessibilityEventMonitorService.PERMISSION_DIALOG_STRINGS.get(permissionText);
                if (permissionString != null) {
                    // found a default android permission rationale string
                    continue;
                }
            }

            // try to match for actual developer provided rationale messages
            if (contextPermissionRationaleKeywords != null && !contextPermissionRationaleKeywords.isEmpty()) {
                for (String keyword : contextPermissionRationaleKeywords) {
                    if (previousScreenText.toLowerCase().contains(keyword.toLowerCase())) {
                        foundRelatedRationale = true;
                        if (!currentlyPreviousScreenContextText.contains(previousScreenText)) {
                            currentlyPreviousScreenContextText = String.format("%s%s", currentlyPreviousScreenContextText, "Screen " + i + "\n" + previousScreenText);
                        }
                        break;
                    }
                }

                if (foundRelatedRationale) {
                    continue;
                }

                HashSet<String> sharedContextPermissionRationaleKeywords = CONTEXT_PERMISSION_RATIONALE_KEY_WORDS.get(PrivaDroidApplication.getAppContext().getString(R.string.android_permission_screen_text_shared));
                if (sharedContextPermissionRationaleKeywords != null) {
                    for (String keyword : sharedContextPermissionRationaleKeywords) {
                        if (previousScreenText.toLowerCase().contains(keyword.toLowerCase())) {
                            foundRelatedRationale = true;
                            if (!currentlyPreviousScreenContextText.contains(previousScreenText)) {
                                currentlyPreviousScreenContextText = String.format("%s%s", currentlyPreviousScreenContextText, "Screen " + i + "\n" + previousScreenText);
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (currentlyPreviousScreenContextText.isEmpty()) {
            currentlyPreviousScreenContextText = NO_CONTEXT_MESSAGE_DETECTED;
        }
    }
}

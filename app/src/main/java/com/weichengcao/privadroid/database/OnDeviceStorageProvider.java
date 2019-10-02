package com.weichengcao.privadroid.database;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.weichengcao.privadroid.PrivaDroidApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.weichengcao.privadroid.database.FirestoreProvider.isNetworkAvailable;

public class OnDeviceStorageProvider {

    private static final String TAG = OnDeviceStorageProvider.class.getSimpleName();

    static final String APP_INSTALL_FILE_NAME = "app_install_events.json";
    static final String APP_UNINSTALL_FILE_NAME = "app_uninstall_events.json";
    static final String PERMISSION_FILE_NAME = "permission_events.json";
    static final String APP_INSTALL_SURVEY_FILE_NAME = "app_install_survey_events.json";
    static final String APP_UNINSTALL_SURVEY_FILE_NAME = "app_uninstall_survey_events.json";
    static final String PERMISSION_GRANT_SURVEY_FILE_NAME = "permission_grant_survey_events.json";
    static final String PERMISSION_DENY_SURVEY_FILE_NAME = "permission_deny_survey_events.json";
    static final String PROACTIVE_PERMISSION_FILE_NAME = "proactive_permission_events.json";

    private static JSONArray readJsonEventsFromFile(String fileName) {
        File file = new File(PrivaDroidApplication.getAppContext().getFilesDir(), fileName);
        try {
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(bytes);
            String jsonString = new String(bytes, StandardCharsets.UTF_8);
            return new JSONArray(jsonString);
        } catch (JSONException | IOException e) {
            return new JSONArray();
        }
    }

    static void writeEventToFile(HashMap<String, String> event, String fileName) {
        File file = new File(PrivaDroidApplication.getAppContext().getFilesDir(), fileName);
        JSONArray savedEvents = new JSONArray();
        if (file.exists()) {
            // 1. read from the file first
            savedEvents = readJsonEventsFromFile(fileName);
        }

        // 2. add new event to savedEvents
        JSONObject newEvent = new JSONObject(event);
        savedEvents.put(newEvent);

        // 2. update file
        try {
            FileOutputStream outputStream = PrivaDroidApplication.getAppContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(savedEvents.toString().getBytes());
            outputStream.close();
            Log.d(TAG, "Write event to disk.");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot find file " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to file " + fileName);
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(PrivaDroidApplication.getAppContext().getFilesDir(), fileName);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    public static HashMap<String, String> eventFromJsonObject(JSONObject jsonObject) {
        HashMap<String, String> mapObj = new Gson().fromJson(
                jsonObject.toString(), new TypeToken<HashMap<String, String>>() {
                }.getType()
        );
        return mapObj;
    }

    public static void syncOnDeviceEventsToFirebase(String fileName) {
        FirestoreProvider firestoreProvider = new FirestoreProvider();

        // Sync app install events
        JSONArray events = readJsonEventsFromFile(fileName);
        if (!deleteFile(fileName)) {
            Log.d(TAG, "Failed to delete event file " + fileName);
            return;
        }
        Log.d(TAG, "Deleted " + fileName);

        int length = events.length();
        for (int i = 0; i < length; i++) {
            try {
                JSONObject object = events.getJSONObject(i);
                HashMap<String, String> map = eventFromJsonObject(object);
                switch (fileName) {
                    case APP_INSTALL_FILE_NAME:
                        firestoreProvider.sendAppInstallEvent(map, false);
                        break;
                    case APP_UNINSTALL_FILE_NAME:
                        firestoreProvider.sendAppUninstallEvent(map, false);
                        break;
                    case APP_INSTALL_SURVEY_FILE_NAME:
                        firestoreProvider.sendAppInstallSurveyEvent(map);
                        break;
                    case APP_UNINSTALL_SURVEY_FILE_NAME:
                        firestoreProvider.sendAppUninstallSurveyEvent(map);
                        break;
                    case PERMISSION_FILE_NAME:
                        firestoreProvider.sendPermissionEvent(map, false);
                        break;
                    case PERMISSION_GRANT_SURVEY_FILE_NAME:
                        firestoreProvider.sendPermissionServerSurveyEvent(map, true);
                        break;
                    case PERMISSION_DENY_SURVEY_FILE_NAME:
                        firestoreProvider.sendPermissionServerSurveyEvent(map, false);
                        break;
                    case PROACTIVE_PERMISSION_FILE_NAME:
                        firestoreProvider.sendProactivePermissionEvent(map);
                        break;
                }
                Log.d(TAG, "Synced event to Firebase.");
            } catch (JSONException ignored) {
            }
        }
    }

    public static void syncAllOnDeviceEventsToFirebase() {
        new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "syncAllOnDeviceEventsToFirebase called.");
                if (isNetworkAvailable()) {
                    Log.d(TAG, "Network available, syncing...");
                    syncOnDeviceEventsToFirebase(APP_INSTALL_FILE_NAME);
                    syncOnDeviceEventsToFirebase(APP_UNINSTALL_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PERMISSION_FILE_NAME);
                    syncOnDeviceEventsToFirebase(APP_INSTALL_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(APP_UNINSTALL_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PERMISSION_GRANT_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PERMISSION_DENY_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PROACTIVE_PERMISSION_FILE_NAME);
                }
            }
        }.run();
    }
}

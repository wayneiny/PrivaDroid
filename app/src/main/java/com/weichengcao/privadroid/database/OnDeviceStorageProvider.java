package com.weichengcao.privadroid.database;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.notifications.ChangePermissionReminderService;
import com.weichengcao.privadroid.util.EventUtil;

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
import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.getJobIdScheduled;
import static com.weichengcao.privadroid.notifications.ChangePermissionReminderService.MAX_DELAY_OF_JOB_IN_MILLISECONDS;

public class OnDeviceStorageProvider extends JobService {

    private static final String TAG = OnDeviceStorageProvider.class.getSimpleName();

    static final String APP_INSTALL_FILE_NAME = "app_install_events.json";
    static final String APP_UNINSTALL_FILE_NAME = "app_uninstall_events.json";
    static final String PERMISSION_FILE_NAME = "permission_events.json";
    static final String APP_INSTALL_SURVEY_FILE_NAME = "app_install_survey_events.json";
    static final String APP_UNINSTALL_SURVEY_FILE_NAME = "app_uninstall_survey_events.json";
    static final String PERMISSION_GRANT_SURVEY_FILE_NAME = "permission_grant_survey_events.json";
    static final String PERMISSION_DENY_SURVEY_FILE_NAME = "permission_deny_survey_events.json";
    static final String PROACTIVE_PERMISSION_FILE_NAME = "proactive_permission_events.json";
    static final String HEARTBEAT_FILE_NAME = "heartbeat_events.json";
    static final String REVOKE_PERMISSION_NOTIFICATION_CLICK_FILE_NAME = "revoke_permission_click_events.json";

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
//            Log.d(TAG, "Write event to disk.");
        } catch (FileNotFoundException e) {
//            Log.e(TAG, "Cannot find file " + fileName);
        } catch (IOException e) {
//            Log.e(TAG, "Failed to write to file " + fileName);
        }
    }

    private static boolean deleteLocalFile(String fileName) {
        File file = new File(PrivaDroidApplication.getAppContext().getFilesDir(), fileName);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    private static HashMap<String, String> eventFromJsonObject(JSONObject jsonObject) {
        return new Gson().fromJson(
                jsonObject.toString(), new TypeToken<HashMap<String, String>>() {
                }.getType()
        );
    }

    static int numberOfEventsSynced = 0;

    private static void syncOnDeviceEventsToFirebase(String fileName) {
        FirestoreProvider firestoreProvider = new FirestoreProvider();

        // Sync app install events
        JSONArray events = readJsonEventsFromFile(fileName);
        if (!deleteLocalFile(fileName)) {
//            Log.d(TAG, "Failed to delete event file " + fileName);
            return;
        }
//        Log.d(TAG, "Deleted " + fileName);

        int length = events.length();
        numberOfEventsSynced += length;
        for (int i = 0; i < length; i++) {
            try {
                JSONObject object = events.getJSONObject(i);
                HashMap<String, String> map = eventFromJsonObject(object);
                map.put(EventUtil.OFFLINE_SYNC, Boolean.toString(true));
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
                    case HEARTBEAT_FILE_NAME:
                        firestoreProvider.sendHeartbeatEvent(map);
                        break;
                    case REVOKE_PERMISSION_NOTIFICATION_CLICK_FILE_NAME:
                        firestoreProvider.sendRevokePermissionNotificationClickEvent(map);
                        break;
                }
//                Log.d(TAG, "Synced event to Firebase.");
            } catch (JSONException ignored) {
            }
        }
    }

    public static void syncAllOnDeviceEventsToFirebase() {
        new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG, "syncAllOnDeviceEventsToFirebase called.");
                if (isNetworkAvailable()) {
//                    Log.d(TAG, "Network available, syncing...");
                    numberOfEventsSynced = 0;
                    syncOnDeviceEventsToFirebase(APP_INSTALL_FILE_NAME);
                    syncOnDeviceEventsToFirebase(APP_UNINSTALL_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PERMISSION_FILE_NAME);
                    syncOnDeviceEventsToFirebase(APP_INSTALL_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(APP_UNINSTALL_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PERMISSION_GRANT_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PERMISSION_DENY_SURVEY_FILE_NAME);
                    syncOnDeviceEventsToFirebase(PROACTIVE_PERMISSION_FILE_NAME);
                    syncOnDeviceEventsToFirebase(HEARTBEAT_FILE_NAME);
                    syncOnDeviceEventsToFirebase(REVOKE_PERMISSION_NOTIFICATION_CLICK_FILE_NAME);
                    FirestoreProvider firestoreProvider = new FirestoreProvider();
                    firestoreProvider.sendLocalStorageSyncLogEvent(ExperimentEventFactory.createLocalStorageSyncEvent(numberOfEventsSynced + ""));
                }
            }
        }.run();
    }

    public static final int LOCAL_STORAGE_SYNC_JOB_ID = 3;
    public static final int ONE_DAY_IN_MILLISECONDS = 86400000;        // 1 day

    public static boolean isLocalStorageSyncJobScheduled() {
        return getCurrentLocalSyncJobScheduled() != null;
    }

    public static JobInfo getCurrentLocalSyncJobScheduled() {
        return getJobIdScheduled(LOCAL_STORAGE_SYNC_JOB_ID);
    }

    public static void scheduleLocalStorageSyncJob() {
        ComponentName serviceComponent = new ComponentName(PrivaDroidApplication.getAppContext(), ChangePermissionReminderService.class);

        JobInfo.Builder builder = new JobInfo.Builder(LOCAL_STORAGE_SYNC_JOB_ID, serviceComponent);
        builder.setMinimumLatency(ONE_DAY_IN_MILLISECONDS);
        builder.setOverrideDeadline(ONE_DAY_IN_MILLISECONDS + MAX_DELAY_OF_JOB_IN_MILLISECONDS);

        JobScheduler jobScheduler = PrivaDroidApplication.getAppContext().getSystemService(JobScheduler.class);
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        syncAllOnDeviceEventsToFirebase();
        scheduleLocalStorageSyncJob();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}

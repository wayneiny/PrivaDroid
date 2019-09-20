package com.weichengcao.privadroid.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.PERMISSION_FILE_NAME;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.DEMOGRAPHIC_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.EVENT_SERVER_ID;
import static com.weichengcao.privadroid.util.EventUtil.KNOW_PERMISSION_REQUIRED;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_COLLECTION;

public class FirestoreProvider {

    private static final String TAG = FirestoreProvider.class.getSimpleName();

    private FirebaseFirestore mFirestore;
    private UserPreferences mUserPreferences;

    public FirestoreProvider() {
        mFirestore = FirebaseFirestore.getInstance();
        mUserPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
    }

    /**
     * Send AppInstallServerEvent to Firebase.
     */
    public void sendAppInstallEvent(final HashMap<String, String> appInstallEvent) {
        // 1. Log a join event and send to FireStore
        mFirestore.collection(APP_INSTALL_COLLECTION).add(appInstallEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        // 2. write to local storage if not successful
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(appInstallEvent, APP_INSTALL_FILE_NAME);
                        }
                    }
                });
    }

    /**
     * Send AppUninstallServerEvent to Firebase.
     */
    public void sendAppUninstallEvent(final HashMap<String, String> appUninstallEvent) {
        // 1. Log a join event and send to FireStore
        mFirestore.collection(APP_UNINSTALL_COLLECTION).add(appUninstallEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        // 2. write to local storage if not successful
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(appUninstallEvent, APP_UNINSTALL_FILE_NAME);
                        }
                    }
                });
    }

    /**
     * Send PermissionServerEvent to Firebase.
     */
    public void sendPermissionEvent(final HashMap<String, String> permissionEvent) {
        // 1. Log a join event and send to FireStore
        mFirestore.collection(PERMISSION_COLLECTION).add(permissionEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        // 2. write to local storage if not successful
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(permissionEvent, PERMISSION_FILE_NAME);
                        }
                    }
                });
    }

    /**
     * Send demographic event to Firebase.
     */
    public void sendDemographicEvent(final HashMap<String, String> demographicEvent) {
        mFirestore.collection(DEMOGRAPHIC_COLLECTION).add(demographicEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            mUserPreferences.setAnsweredDemographicSurvey(true);
                        } else {
                            mUserPreferences.setAnsweredDemographicSurvey(false);
                            OnDeviceStorageProvider.writeDemographicEventToFile(demographicEvent);
                        }
                    }
                });
    }

    /**
     * Send app install survey event to Firebase.
     */
    public void sendAppInstallSurveyEvent(final HashMap<String, String> appInstallSurvey) {
        mFirestore.collection(APP_INSTALL_SURVEY_COLLECTION).add(appInstallSurvey)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(appInstallSurvey, APP_INSTALL_SURVEY_FILE_NAME);
                        } else {
                            /**
                             * 1. Get the server app install survey document.
                             */
                            DocumentReference doc = task.getResult();
                            final AppInstallServerEvent appInstallServerEvent = (AppInstallServerEvent) PrivaDroidApplication.serverId2appInstallServerUnsurveyedEvents.get(appInstallSurvey.get(EVENT_SERVER_ID));
                            if (appInstallServerEvent == null || doc == null) {
                                return;
                            }

                            /**
                             * 2. Create AppInstallServerSurvey and put in hash map.
                             */
                            AppInstallServerSurvey appInstallServerSurvey = new AppInstallServerSurvey(
                                    appInstallSurvey.get(EventUtil.USER_AD_ID), appInstallSurvey.get(EventUtil.LOGGED_TIME),
                                    EventUtil.APP_INSTALL_EVENT_TYPE, appInstallSurvey.get(EventUtil.WHY_INSTALL),
                                    appInstallSurvey.get(EventUtil.INSTALL_FACTORS), appInstallSurvey.get(KNOW_PERMISSION_REQUIRED),
                                    appInstallSurvey.get(EventUtil.PERMISSIONS_THINK_REQUIRED), appInstallServerEvent.getServerId(),
                                    doc.getId());
                            PrivaDroidApplication.serverId2appInstallSurveys.put(doc.getId(), appInstallServerSurvey);

                            /**
                             * 3. Update its corresponding app install event.
                             */
                            appInstallServerEvent.setSurveyId(doc.getId());
                            mFirestore.collection(APP_INSTALL_COLLECTION).document(appInstallServerEvent.getServerId())
                                    .update(createAppInstallEventHashMapFromObject(appInstallServerEvent))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                /**
                                                 * Move this app install event to surveyed map.
                                                 */
                                                PrivaDroidApplication.serverId2appInstallServerUnsurveyedEvents.remove(appInstallServerEvent.getServerId());
                                                PrivaDroidApplication.serverId2appInstallServerSurveyedEvents.put(appInstallServerEvent.getServerId(), appInstallServerEvent);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Send app uninstall survey event to Firebase.
     */
    public void sendAppUninstallSurveyEvent(final HashMap<String, String> appUninstallSurvey) {
        mFirestore.collection(APP_UNINSTALL_SURVEY_COLLECTION).add(appUninstallSurvey)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(appUninstallSurvey, APP_UNINSTALL_SURVEY_FILE_NAME);
                        } else {
                            /**
                             * 1. Get the server app uninstall survey document.
                             */
                            DocumentReference doc = task.getResult();
                            final AppUninstallServerEvent appUninstallServerEvent = (AppUninstallServerEvent) PrivaDroidApplication.serverId2appUninstallServerUnsurveyedEvents.get(appUninstallSurvey.get(EVENT_SERVER_ID));
                            if (appUninstallServerEvent == null || doc == null) {
                                return;
                            }

                            /**
                             * 2. Create AppUninstallServerSurvey and put in hash map.
                             */
                            AppUninstallServerSurvey appUninstallServerSurvey = new AppUninstallServerSurvey(
                                    appUninstallSurvey.get(EventUtil.USER_AD_ID), appUninstallSurvey.get(EventUtil.LOGGED_TIME),
                                    EventUtil.APP_UNINSTALL_EVENT_TYPE, appUninstallSurvey.get(EventUtil.WHY_UNINSTALL),
                                    appUninstallSurvey.get(EventUtil.PERMISSION_REMEMBERED_REQUESTED), appUninstallServerEvent.getServerId(),
                                    doc.getId());
                            PrivaDroidApplication.serverId2appUninstallSurveys.put(doc.getId(), appUninstallServerSurvey);

                            /**
                             * 3. Update its corresponding app uninstall event.
                             */
                            appUninstallServerEvent.setSurveyId(doc.getId());
                            mFirestore.collection(APP_UNINSTALL_COLLECTION).document(appUninstallServerEvent.getServerId())
                                    .update(createAppUninstallEventHashMapFromObject(appUninstallServerEvent))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                /**
                                                 * Move this app install event to surveyed map.
                                                 */
                                                PrivaDroidApplication.serverId2appUninstallServerUnsurveyedEvents.remove(appUninstallServerEvent.getServerId());
                                                PrivaDroidApplication.serverId2appUninstallServerSurveyedEvents.put(appUninstallServerEvent.getServerId(), appUninstallServerEvent);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Transformation from AppUninstallServerEvent to HashMap.
     */
    private static HashMap<String, Object> createAppUninstallEventHashMapFromObject(AppUninstallServerEvent event) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(EventUtil.USER_AD_ID, event.getAdId());
        map.put(EventUtil.LOGGED_TIME, event.getLoggedTime());
        map.put(EventUtil.APP_NAME, event.getAppName());
        map.put(EventUtil.PACKAGE_NAME, event.getPackageName());
        map.put(EventUtil.APP_VERSION, event.getAppVersion());
        map.put(EventUtil.SURVEY_ID, event.getSurveyId());

        return map;
    }

    /**
     * Transformation from AppInstallServerEvent to HashMap.
     */
    private static HashMap<String, Object> createAppInstallEventHashMapFromObject(AppInstallServerEvent event) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(EventUtil.USER_AD_ID, event.getAdId());
        map.put(EventUtil.LOGGED_TIME, event.getLoggedTime());
        map.put(EventUtil.APP_NAME, event.getAppName());
        map.put(EventUtil.PACKAGE_NAME, event.getPackageName());
        map.put(EventUtil.APP_VERSION, event.getAppVersion());
        map.put(EventUtil.SURVEY_ID, event.getSurveyId());

        return map;
    }
}

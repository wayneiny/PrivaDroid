package com.weichengcao.privadroid.database;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.notifications.MarshmallowNotificationProvider;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.PERMISSION_FILE_NAME;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.DEMOGRAPHIC_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.EVENT_SERVER_ID;
import static com.weichengcao.privadroid.util.EventUtil.PACKAGE_NAME;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.SURVEY_ID;

public class FirestoreProvider {

    private static final String TAG = FirestoreProvider.class.getSimpleName();

    private FirebaseFirestore mFirestore;
    private UserPreferences mUserPreferences;
    private Context mContext;

    public FirestoreProvider() {
        mFirestore = FirebaseFirestore.getInstance();
        mUserPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
        mContext = PrivaDroidApplication.getAppContext();
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
                        } else {
                            DocumentReference doc = task.getResult();
                            if (doc == null) {
                                return;
                            }

                            /**
                             * Create AppInstallServerEvent.
                             */
                            AppInstallServerEvent event = new AppInstallServerEvent(doc.getId(),
                                    appInstallEvent.get(EventUtil.USER_AD_ID), appInstallEvent.get(EventUtil.APP_NAME),
                                    appInstallEvent.get(EventUtil.APP_VERSION), appInstallEvent.get(EventUtil.LOGGED_TIME),
                                    appInstallEvent.get(EventUtil.PACKAGE_NAME), appInstallEvent.get(EventUtil.SURVEY_ID),
                                    APP_INSTALL_EVENT_TYPE);

                            /**
                             * Create notification for users to answer based on Android version.
                             */
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                                new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            }
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
                        } else {
                            DocumentReference doc = task.getResult();
                            if (doc == null) {
                                return;
                            }

                            /**
                             * Create AppUninstallServerEvent.
                             */
                            AppUninstallServerEvent event = new AppUninstallServerEvent(doc.getId(),
                                    appUninstallEvent.get(EventUtil.USER_AD_ID), appUninstallEvent.get(EventUtil.APP_NAME),
                                    appUninstallEvent.get(EventUtil.APP_VERSION), appUninstallEvent.get(EventUtil.LOGGED_TIME),
                                    appUninstallEvent.get(PACKAGE_NAME), appUninstallEvent.get(SURVEY_ID),
                                    APP_UNINSTALL_EVENT_TYPE);

                            /**
                             * Create notification for users to answer based on Android version.
                             */
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                                new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            }
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
                             * 2. Query Firebase for the corresponding install event.
                             */
                            final DocumentReference surveyDoc = task.getResult();
                            CollectionReference appInstallEventCollectionRef = FirebaseFirestore.getInstance().collection(APP_INSTALL_COLLECTION);
                            DocumentReference eventDocRef = appInstallEventCollectionRef.document(appInstallSurvey.get(EVENT_SERVER_ID));
                            eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot eventDoc = task.getResult();
                                        if (eventDoc.exists()) {
                                            final AppInstallServerEvent appInstallServerEvent = new AppInstallServerEvent(eventDoc.getId(),
                                                    eventDoc.getString(EventUtil.USER_AD_ID), eventDoc.getString(EventUtil.APP_NAME),
                                                    eventDoc.getString(EventUtil.APP_VERSION), eventDoc.getString(EventUtil.LOGGED_TIME),
                                                    eventDoc.getString(EventUtil.PACKAGE_NAME), eventDoc.getString(EventUtil.SURVEY_ID),
                                                    APP_INSTALL_EVENT_TYPE);

                                            /**
                                             * 3. Update its corresponding app install event.
                                             */
                                            appInstallServerEvent.setSurveyId(surveyDoc.getId());
                                            mFirestore.collection(APP_INSTALL_COLLECTION).document(appInstallServerEvent.getServerId())
                                                    .update(createAppInstallEventHashMapFromObject(appInstallServerEvent));
                                        }
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
                             * 2. Query Firebase for the corresponding uninstall event.
                             */
                            final DocumentReference surveyDoc = task.getResult();
                            CollectionReference appInstallEventCollectionRef = FirebaseFirestore.getInstance().collection(APP_UNINSTALL_COLLECTION);
                            DocumentReference eventDocRef = appInstallEventCollectionRef.document(appUninstallSurvey.get(EVENT_SERVER_ID));
                            eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot eventDoc = task.getResult();
                                        if (eventDoc.exists()) {
                                            final AppUninstallServerEvent appUninstallServerEvent = new AppUninstallServerEvent(eventDoc.getId(),
                                                    eventDoc.getString(EventUtil.USER_AD_ID), eventDoc.getString(EventUtil.APP_NAME),
                                                    eventDoc.getString(EventUtil.APP_VERSION), eventDoc.getString(EventUtil.LOGGED_TIME),
                                                    eventDoc.getString(PACKAGE_NAME), eventDoc.getString(SURVEY_ID),
                                                    APP_UNINSTALL_EVENT_TYPE);

                                            /**
                                             * 3. Update its corresponding app uninstall event.
                                             */
                                            appUninstallServerEvent.setSurveyId(surveyDoc.getId());
                                            mFirestore.collection(APP_UNINSTALL_COLLECTION).document(appUninstallServerEvent.getServerId())
                                                    .update(createAppUninstallEventHashMapFromObject(appUninstallServerEvent));
                                        }
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

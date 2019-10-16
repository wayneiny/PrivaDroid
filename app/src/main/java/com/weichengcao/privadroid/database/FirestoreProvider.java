package com.weichengcao.privadroid.database;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.weichengcao.privadroid.notifications.NougatMR1NotificationProvider;
import com.weichengcao.privadroid.notifications.NougatNotificationProvider;
import com.weichengcao.privadroid.notifications.OreoMR1NotificationProvider;
import com.weichengcao.privadroid.notifications.OreoNotificationProvider;
import com.weichengcao.privadroid.notifications.PieNotificationProvider;
import com.weichengcao.privadroid.notifications.QNotificationProvider;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.PrivaDroidApplication.getAppContext;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.PERMISSION_DENY_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.PERMISSION_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.PERMISSION_GRANT_SURVEY_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.PROACTIVE_PERMISSION_FILE_NAME;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.DEMOGRAPHIC_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.EVENT_SERVER_ID;
import static com.weichengcao.privadroid.util.EventUtil.EXIT_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.PACKAGE_NAME;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.SURVEY_ID;
import static com.weichengcao.privadroid.util.UserPreferences.UNKNOWN_DATE;

public class FirestoreProvider {

    private static final String TAG = FirestoreProvider.class.getSimpleName();

    private FirebaseFirestore mFirestore;
    private UserPreferences mUserPreferences;

    public FirestoreProvider() {
        mFirestore = FirebaseFirestore.getInstance();
        mUserPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
    }

    /**
     * Send exit survey event to Firebase.
     */
    public void sendExitSurveyEvent(final HashMap<String, String> exitSurvey) {
        mFirestore.collection(EXIT_SURVEY_COLLECTION).add(exitSurvey);
    }

    /**
     * Send RewardsServerEvent to Firebase.
     */
    public void sendRewardsEvent(final HashMap<String, String> rewardsEvent) {
        if (new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate().equals(UNKNOWN_DATE)) {
            return;
        }

        mFirestore.collection(EventUtil.REWARDS_COLLECTION).add(rewardsEvent);
    }

    /**
     * Send AppInstallServerEvent to Firebase.
     */
    public void sendAppInstallEvent(final HashMap<String, String> appInstallEvent, final boolean createNotificationForSurvey) {
        if (new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate().equals(UNKNOWN_DATE)) {
            return;
        }

        if (!isNetworkAvailable()) {
            OnDeviceStorageProvider.writeEventToFile(appInstallEvent, APP_INSTALL_FILE_NAME);
            return;
        }

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
                            if (!createNotificationForSurvey) {
                                return;
                            }

                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                                new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                                new NougatNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                new NougatMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                                new OreoNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
                                new OreoMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                                new PieNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                                new QNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForInstallEventSurvey(event);
                            }
                        }
                    }
                });
    }

    /**
     * Send AppUninstallServerEvent to Firebase.
     */
    public void sendAppUninstallEvent(final HashMap<String, String> appUninstallEvent, final boolean createNotificationForSurvey) {
        if (new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate().equals(UNKNOWN_DATE)) {
            return;
        }

        if (!isNetworkAvailable()) {
            OnDeviceStorageProvider.writeEventToFile(appUninstallEvent, APP_UNINSTALL_FILE_NAME);
            return;
        }

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
                            if (!createNotificationForSurvey) {
                                return;
                            }

                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                                new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                                new NougatNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                new NougatMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                                new OreoNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
                                new OreoMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                                new PieNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                                new QNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForUninstallEventSurvey(event);
                            }
                        }
                    }
                });
    }

    /**
     * Send ProactivePermissionServerEvent to Firebase.
     */
    public void sendProactivePermissionEvent(final HashMap<String, String> proactivePermissionEvent) {
        if (new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate().equals(UNKNOWN_DATE)) {
            return;
        }

        if (!isNetworkAvailable()) {
            OnDeviceStorageProvider.writeEventToFile(proactivePermissionEvent, PROACTIVE_PERMISSION_FILE_NAME);
            return;
        }

        mFirestore.collection(EventUtil.PROACTIVE_RATIONALE_COLLECTION).add(proactivePermissionEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(proactivePermissionEvent, PROACTIVE_PERMISSION_FILE_NAME);
                        }
                    }
                });
    }

    /**
     * Send PermissionServerEvent to Firebase.
     */
    public void sendPermissionEvent(final HashMap<String, String> permissionEvent, final boolean createNotificationForSurvey) {
        if (new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate().equals(UNKNOWN_DATE)) {
            return;
        }

        if (!isNetworkAvailable()) {
            OnDeviceStorageProvider.writeEventToFile(permissionEvent, PERMISSION_FILE_NAME);
            return;
        }

        // 1. Log a join event and send to FireStore
        mFirestore.collection(PERMISSION_COLLECTION).add(permissionEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        // 2. write to local storage if not successful
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(permissionEvent, PERMISSION_FILE_NAME);
                        } else {
                            DocumentReference doc = task.getResult();
                            if (doc == null) {
                                return;
                            }

                            /**
                             * Create PermissionGrantServerEvent or PermissionDenyServerEvent.
                             */
                            PermissionServerEvent event = new PermissionServerEvent(doc.getId(), permissionEvent.get(EventUtil.USER_AD_ID),
                                    permissionEvent.get(EventUtil.APP_NAME), permissionEvent.get(EventUtil.APP_VERSION),
                                    permissionEvent.get(EventUtil.LOGGED_TIME), permissionEvent.get(PACKAGE_NAME),
                                    permissionEvent.get(SURVEY_ID), PERMISSION_EVENT_TYPE,
                                    permissionEvent.get(EventUtil.INITIATED_BY_USER), permissionEvent.get(EventUtil.PERMISSION_REQUESTED_NAME),
                                    permissionEvent.get(EventUtil.GRANTED));

                            /**
                             * Create notification for users to answer based on Android version.
                             */
                            if (!createNotificationForSurvey) {
                                return;
                            }

                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                                new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForPermissionEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                                new NougatNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForPermissionEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                                new NougatMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForPermissionEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                                new OreoNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForPermissionEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
                                new OreoMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForPermissionEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                                new PieNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForPermissionEventSurvey(event);
                            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                                new QNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForPermissionEventSurvey(event);
                            }
                        }
                    }
                });
    }

    /**
     * Send PermissionGrantServerSurvey to Firebase.
     */
    public void sendPermissionServerSurveyEvent(final HashMap<String, String> permissionGrantSurvey, boolean isGrantSurvey) {
        if (permissionGrantSurvey == null || permissionGrantSurvey.get(EVENT_SERVER_ID) == null) {
            return;
        }

        final String surveyLocalFile = isGrantSurvey ? PERMISSION_GRANT_SURVEY_FILE_NAME : PERMISSION_DENY_SURVEY_FILE_NAME;
        if (!isNetworkAvailable()) {
            OnDeviceStorageProvider.writeEventToFile(permissionGrantSurvey, surveyLocalFile);
            return;
        }

        String surveyCollection = isGrantSurvey ? EventUtil.PERMISSION_GRANT_SURVEY_COLLECTION : EventUtil.PERMISSION_DENY_SURVEY_COLLECTION;
        mFirestore.collection(surveyCollection).add(permissionGrantSurvey)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            OnDeviceStorageProvider.writeEventToFile(permissionGrantSurvey, surveyLocalFile);
                        } else {
                            /**
                             * 1. Get the server permission grant survey document.
                             * 2. Query Firebase for the corresponding permission event.
                             */
                            final DocumentReference surveyDoc = task.getResult();
                            CollectionReference permissionEventCollectionRef = FirebaseFirestore.getInstance().collection(PERMISSION_COLLECTION);
                            DocumentReference eventDocRef = permissionEventCollectionRef.document(permissionGrantSurvey.get(EVENT_SERVER_ID));
                            eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot eventDoc = task.getResult();
                                        if (eventDoc != null && eventDoc.exists()) {
                                            final PermissionServerEvent permissionServerEvent;
                                            if (surveyDoc != null) {
                                                permissionServerEvent = new PermissionServerEvent(eventDoc.getId(),
                                                        eventDoc.getString(EventUtil.USER_AD_ID), eventDoc.getString(EventUtil.APP_NAME),
                                                        eventDoc.getString(EventUtil.APP_VERSION), eventDoc.getString(EventUtil.LOGGED_TIME),
                                                        eventDoc.getString(PACKAGE_NAME), surveyDoc.getId(), PERMISSION_EVENT_TYPE,
                                                        eventDoc.getString(EventUtil.INITIATED_BY_USER), eventDoc.getString(EventUtil.PERMISSION_REQUESTED_NAME),
                                                        eventDoc.getString(EventUtil.GRANTED));

                                                /**
                                                 * 3. Update its corresponding permission event.
                                                 */
                                                mFirestore.collection(PERMISSION_COLLECTION).document(permissionServerEvent.getServerId())
                                                        .update(createPermissionServerEventHashMapFromObject(permissionServerEvent));
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
    }

    /**
     * Send demographic event to Firebase.
     */
    public void sendDemographicEvent(final HashMap<String, String> demographicEvent) {
        mFirestore.collection(DEMOGRAPHIC_COLLECTION).add(demographicEvent);
    }

    /**
     * Send app install survey event to Firebase.
     */
    public void sendAppInstallSurveyEvent(final HashMap<String, String> appInstallSurvey) {
        if (appInstallSurvey == null || appInstallSurvey.get(EVENT_SERVER_ID) == null) {
            return;
        }

        if (!isNetworkAvailable()) {
            OnDeviceStorageProvider.writeEventToFile(appInstallSurvey, APP_INSTALL_SURVEY_FILE_NAME);
            return;
        }

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
                                        if (eventDoc != null && eventDoc.exists()) {
                                            final AppInstallServerEvent appInstallServerEvent;
                                            if (surveyDoc != null) {
                                                appInstallServerEvent = new AppInstallServerEvent(eventDoc.getId(),
                                                        eventDoc.getString(EventUtil.USER_AD_ID), eventDoc.getString(EventUtil.APP_NAME),
                                                        eventDoc.getString(EventUtil.APP_VERSION), eventDoc.getString(EventUtil.LOGGED_TIME),
                                                        eventDoc.getString(EventUtil.PACKAGE_NAME), surveyDoc.getId(),
                                                        APP_INSTALL_EVENT_TYPE);

                                                /**
                                                 * 3. Update its corresponding app install event.
                                                 */
                                                mFirestore.collection(APP_INSTALL_COLLECTION).document(appInstallServerEvent.getServerId())
                                                        .update(createAppInstallEventHashMapFromObject(appInstallServerEvent));
                                            }
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
        if (appUninstallSurvey == null || appUninstallSurvey.get(EVENT_SERVER_ID) == null) {
            return;
        }

        if (!isNetworkAvailable()) {
            OnDeviceStorageProvider.writeEventToFile(appUninstallSurvey, APP_UNINSTALL_SURVEY_FILE_NAME);
            return;
        }

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
                                        if (eventDoc != null && eventDoc.exists()) {
                                            final AppUninstallServerEvent appUninstallServerEvent;
                                            if (surveyDoc != null) {
                                                appUninstallServerEvent = new AppUninstallServerEvent(eventDoc.getId(),
                                                        eventDoc.getString(EventUtil.USER_AD_ID), eventDoc.getString(EventUtil.APP_NAME),
                                                        eventDoc.getString(EventUtil.APP_VERSION), eventDoc.getString(EventUtil.LOGGED_TIME),
                                                        eventDoc.getString(PACKAGE_NAME), surveyDoc.getId(),
                                                        APP_UNINSTALL_EVENT_TYPE);

                                                /**
                                                 * 3. Update its corresponding app uninstall event.
                                                 */
                                                mFirestore.collection(APP_UNINSTALL_COLLECTION).document(appUninstallServerEvent.getServerId())
                                                        .update(createAppUninstallEventHashMapFromObject(appUninstallServerEvent));
                                            }
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

    /**
     * Transformation from PermissionServerEvent to HashMap.
     */
    private static HashMap<String, Object> createPermissionServerEventHashMapFromObject(PermissionServerEvent event) {
        HashMap<String, Object> map = new HashMap<>();

        map.put(EventUtil.USER_AD_ID, event.getAdId());
        map.put(EventUtil.LOGGED_TIME, event.getLoggedTime());
        map.put(EventUtil.APP_NAME, event.getAppName());
        map.put(EventUtil.PACKAGE_NAME, event.getPackageName());
        map.put(EventUtil.APP_VERSION, event.getAppVersion());
        map.put(EventUtil.INITIATED_BY_USER, event.getInitiatedByUser());
        map.put(EventUtil.PERMISSION_REQUESTED_NAME, event.getPermissionName());
        map.put(EventUtil.GRANTED, event.getRequestGranted());
        map.put(EventUtil.SURVEY_ID, event.getSurveyId());

        return map;
    }

    /**
     * Check if the Android is connected to the network.
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

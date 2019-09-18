package com.weichengcao.privadroid.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.util.EventConstants;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.ArrayList;
import java.util.HashMap;

import static com.weichengcao.privadroid.PrivaDroidApplication.appInstallServerSurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.appInstallServerUnsurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.appUninstallServerSurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.appUninstallServerUnsurveyedEvents;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.PERMISSION_FILE_NAME;
import static com.weichengcao.privadroid.util.EventConstants.APP_INSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventConstants.APP_UNINSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventConstants.PERMISSION_COLLECTION;

public class FirestoreProvider {

    private static final String TAG = FirestoreProvider.class.getSimpleName();

    private FirebaseFirestore mFirestore;
    private UserPreferences mUserPreferences;

    public FirestoreProvider() {
        mFirestore = FirebaseFirestore.getInstance();
        mUserPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
    }

    // TODO: probably can collapse three methods into one?
    // send app install event [START]
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
    // send app install event [END]

    // send app uninstall event [START]
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
    // send app uninstall event [END]

    // send permission event [START]
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
    // send permission event [END]

    /**
     * Query app install events for single user and update appInstallServerSurveyedEvents and appInstallServerUnsurveyedEvents.
     */
    public void queryFirestoreAppInstallEventsForUser(String adId) {
        CollectionReference collectionReference = mFirestore.collection(APP_INSTALL_COLLECTION);
        Query query = collectionReference.whereEqualTo(EventConstants.USER_AD_ID, adId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    appInstallServerSurveyedEvents = new ArrayList<>();
                    appInstallServerUnsurveyedEvents = new ArrayList<>();

                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String adId = document.getString(EventConstants.USER_AD_ID);
                            String appName = document.getString(EventConstants.APP_NAME);
                            String appVersion = document.getString(EventConstants.APP_VERSION);
                            String loggedTime = document.getString(EventConstants.LOGGED_TIME);
                            String packageName = document.getString(EventConstants.PACKAGE_NAME);
                            String surveyed = document.getString(EventConstants.SURVEYED);
                            AppInstallServerEvent event = new AppInstallServerEvent(adId, appName, appVersion, loggedTime, packageName, surveyed);

                            if (event.isEventSurveyed()) {
                                appInstallServerSurveyedEvents.add(event);
                            } else {
                                appInstallServerUnsurveyedEvents.add(event);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Query app uninstall events for single user and update appUninstallServerSurveyedEvents and appUninstallServerUnsurveyedEvents.
     */
    public void queryFirestoreAppUninstallEventsForUser(String adId) {
        CollectionReference collectionReference = mFirestore.collection(APP_UNINSTALL_COLLECTION);
        Query query = collectionReference.whereEqualTo(EventConstants.USER_AD_ID, adId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    appUninstallServerSurveyedEvents = new ArrayList<>();
                    appUninstallServerUnsurveyedEvents = new ArrayList<>();

                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String adId = document.getString(EventConstants.USER_AD_ID);
                            String appName = document.getString(EventConstants.APP_NAME);
                            String appVersion = document.getString(EventConstants.APP_VERSION);
                            String loggedTime = document.getString(EventConstants.LOGGED_TIME);
                            String packageName = document.getString(EventConstants.PACKAGE_NAME);
                            String surveyed = document.getString(EventConstants.SURVEYED);
                            AppUninstallServerEvent event = new AppUninstallServerEvent(adId, appName, appVersion, loggedTime, packageName, surveyed);

                            if (event.isEventSurveyed()) {
                                appUninstallServerSurveyedEvents.add(event);
                            } else {
                                appUninstallServerUnsurveyedEvents.add(event);
                            }
                        }
                    }
                }
            }
        });
    }
}

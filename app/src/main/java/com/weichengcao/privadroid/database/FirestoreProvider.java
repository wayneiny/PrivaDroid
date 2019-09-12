package com.weichengcao.privadroid.database;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_INSTALL_FILE_NAME;
import static com.weichengcao.privadroid.database.OnDeviceStorageProvider.APP_UNINSTALL_FILE_NAME;
import static com.weichengcao.privadroid.util.EventConstants.APP_INSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventConstants.APP_UNINSTALL_COLLECTION;

public class FirestoreProvider {

    private Context mContext;
    private FirebaseFirestore mFirestore;
    private UserPreferences mUserPreferences;

    public FirestoreProvider() {
        mContext = PrivaDroidApplication.getAppContext();
        mFirestore = FirebaseFirestore.getInstance();
        mUserPreferences = new UserPreferences(mContext);
    }

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
}

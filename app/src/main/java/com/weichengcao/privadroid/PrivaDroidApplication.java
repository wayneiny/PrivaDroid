package com.weichengcao.privadroid;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.weichengcao.privadroid.database.BaseServerEvent;
import com.weichengcao.privadroid.database.BaseServerSurvey;
import com.weichengcao.privadroid.database.DemographicEvent;

import java.util.HashMap;

import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;

public class PrivaDroidApplication extends Application {

    public static final String FIREBASE_PROJECT_ALIAS = "firebase_project_name";
    public static final String FIREBASE_PROJECT_APPLICATION_ID = "1:9488278665:android:f73dbd4e02622c756f1258";
    public static final String FIREBASE_PROJECT_API_KEY = "AIzaSyCM0V2tMA3a9X4uQB7RKQ04-HIDfwu9OxQ";
    public static final String FIREBASE_PROJECT_DATABASE_URL = "https://privadroid-test-2e6f8.firebaseio.com";
    public static final String FIREBASE_PROJECT_ID = "privadroid-test-2e6f8";

    private static PrivaDroidApplication instance;
    private static Context appContext;

    public static PrivaDroidApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context mAppContext) {
        appContext = mAppContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        this.setAppContext(getApplicationContext());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        /**
         * Initialize Firebase project.
         */
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId(FIREBASE_PROJECT_APPLICATION_ID)
                .setApiKey(FIREBASE_PROJECT_API_KEY)
                .setDatabaseUrl(FIREBASE_PROJECT_DATABASE_URL)
                .setProjectId(FIREBASE_PROJECT_ID)
                .build();
        FirebaseApp.initializeApp(this /* Context */, options, FIREBASE_PROJECT_ALIAS);
    }

    /**
     * Store currently handled event type.
     */
    private static int currentlyHandledEventType = APP_INSTALL_EVENT_TYPE;

    public static void setCurrentlyHandledEventType(int eventType) {
        currentlyHandledEventType = eventType;
    }

    public static int getCurrentlyHandledEventType() {
        return currentlyHandledEventType;
    }

    /**
     * Store queried app install, demographic, app uninstall and permission data.
     */
    public static HashMap<String, BaseServerEvent> serverId2appInstallServerSurveyedEvents = new HashMap<>();
    public static HashMap<String, BaseServerEvent> serverId2appInstallServerUnsurveyedEvents = new HashMap<>();

    public static HashMap<String, BaseServerEvent> serverId2appUninstallServerSurveyedEvents = new HashMap<>();
    public static HashMap<String, BaseServerEvent> serverId2appUninstallServerUnsurveyedEvents = new HashMap<>();

    public static HashMap<String, BaseServerEvent> serverId2permissionServerSurveyedEvents = new HashMap<>();
    public static HashMap<String, BaseServerEvent> serverId2permissionServerUnsurveyedEvents = new HashMap<>();

    public static HashMap<String, BaseServerSurvey> serverId2appInstallSurveys = new HashMap<>();
    public static HashMap<String, BaseServerSurvey> serverId2appUninstallSurveys = new HashMap<>();
    public static HashMap<String, BaseServerSurvey> serverId2permissionSurveys = new HashMap<>();
}

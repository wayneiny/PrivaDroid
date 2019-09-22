package com.weichengcao.privadroid;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.weichengcao.privadroid.database.BaseServerEvent;
import com.weichengcao.privadroid.database.BaseServerSurvey;
import com.weichengcao.privadroid.database.DemographicEvent;

import java.util.HashMap;

import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;

public class PrivaDroidApplication extends Application {

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

    public static DemographicEvent demographicEvent = null;
}

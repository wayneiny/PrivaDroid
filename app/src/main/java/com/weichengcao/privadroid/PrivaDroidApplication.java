package com.weichengcao.privadroid;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.weichengcao.privadroid.database.BaseServerEvent;
import com.weichengcao.privadroid.database.DemographicEvent;

import java.util.HashMap;

import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;

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
    private static int currentyHandledEventType = APP_INSTALL_EVENT_TYPE;

    public static void setCurrentyHandledEventType(int eventType) {
        currentyHandledEventType = eventType;
    }

    public static int getCurrentyHandledEventType() {
        return currentyHandledEventType;
    }

    /**
     * TODO: Store queried app install, demographic, app uninstall and permission data.
     */
    public static HashMap<String, BaseServerEvent> serverId2appInstallServerSurveyedEvents = new HashMap<>();
    public static HashMap<String, BaseServerEvent> serverId2appInstallServerUnsurveyedEvents = new HashMap<>();

    public static HashMap<String, BaseServerEvent> serverId2appUninstallServerSurveyedEvents = new HashMap<>();
    public static HashMap<String, BaseServerEvent> serverId2appUninstallServerUnsurveyedEvents = new HashMap<>();

    public static DemographicEvent demographicEvent = null;
}

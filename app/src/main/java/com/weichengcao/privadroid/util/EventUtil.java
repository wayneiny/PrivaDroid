package com.weichengcao.privadroid.util;

import com.weichengcao.privadroid.database.BaseServerEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2appInstallServerSurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2appInstallServerUnsurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2appUninstallServerSurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2appUninstallServerUnsurveyedEvents;
import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_UNINSTALL_EVENT_TYPE;

public class EventUtil {
    // common
    public static final String USER_AD_ID = "ad_id";
    public static final String LOGGED_TIME = "logged_time";
    public static final String APP_NAME = "app_name";
    public static final String PACKAGE_NAME = "package_name";
    public static final String APP_VERSION = "app_version";
    public static final String SURVEY_ID = "survey_id";
    public static final String SYNCED = "synced";   // synced to Firestore storage, only used in local storage

    // join event
    public static final String JOIN_EVENT_COLLECTION = "JOIN_EVENT_COLLECTION";
    public static final String PHONE_MAKE = "make";
    public static final String PHONE_MODEL = "model";
    public static final String ANDROID_VERSION = "android_version";
    public static final String CARRIER = "carrier";

    // app install
    public static final String APP_INSTALL_COLLECTION = "APP_INSTALL_COLLECTION";

    // app uninstall
    public static final String APP_UNINSTALL_COLLECTION = "APP_UNINSTALL_COLLECTION";

    // permission
    public static final String PERMISSION_COLLECTION = "PERMISSION_COLLECTION";
    public static final String PERMISSION_REQUESTED_NAME = "permission_requested";
    public static final String GRANTED = "granted";
    public static final String INITIATED_BY_USER = "user_initiated";

    /**
     * Demographic event.
     */
    public static final String DEMOGRAPHIC_COLLECTION = "DEMOGRAPHIC_COLLECTION";
    public static final String EDUCATION = "education";
    public static final String INCOME = "income";
    public static final String AGE = "age";
    public static final String GENDER = "gender";
    public static final String INDUSTRY = "industry";
    public static final String DAILY_USAGE= "daily_usage";
    public static final String STATUS = "status";
    public static final String COUNTRY = "country";

    /**
     * Event utility functions.
     */
    public static ArrayList<BaseServerEvent> sortEventsBasedOnTime(HashMap<String, BaseServerEvent> eventsMap, final boolean chronological) {
        ArrayList<BaseServerEvent> res = new ArrayList<>(eventsMap.values());
        Collections.sort(res, new Comparator<BaseServerEvent>() {
            @Override
            public int compare(BaseServerEvent s1, BaseServerEvent s2) {
                boolean s1LaterThans2 = DatetimeUtil.aLaterThanBIso(s1.getLoggedTime(), s2.getLoggedTime());
                if (chronological) {
                    return s1LaterThans2 ? 1 : 0;
                } else {
                    return s1LaterThans2 ? 0 : 1;
                }
            }
        });
        return res;
    }

    /**
     * Get correct hashmap based on event type.
     */
    public static final int SURVEYED_EVENT = 0;
    public static final int UNSURVEYED_EVENT = 1;

    public static HashMap<String, BaseServerEvent> getProperDataHashMap(int eventType, int surveyedType) {
        switch (eventType) {
            case APP_INSTALL_EVENT_TYPE:
                return surveyedType == SURVEYED_EVENT ? serverId2appInstallServerSurveyedEvents :
                        serverId2appInstallServerUnsurveyedEvents;
            case APP_UNINSTALL_EVENT_TYPE:
                return surveyedType == SURVEYED_EVENT ? serverId2appUninstallServerSurveyedEvents :
                        serverId2appUninstallServerUnsurveyedEvents;
            default:
                return null;
        }
    }
}

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
import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2permissionServerSurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2permissionServerUnsurveyedEvents;

public class EventUtil {
    /**
     * Intent keys.
     */
    public static final String EVENT_ID_INTENT_KEY = "EVENT_ID_INTENT_KEY";

    /**
     * Common to App Install/App Uninstall/Permission.
     */
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final int APP_INSTALL_EVENT_TYPE = 0;
    public static final int APP_UNINSTALL_EVENT_TYPE = 1;
    public static final int PERMISSION_EVENT_TYPE = 2;
    public static final String USER_AD_ID = "ad_id";
    public static final String LOGGED_TIME = "logged_time";
    public static final String APP_NAME = "app_name";
    public static final String PACKAGE_NAME = "package_name";
    public static final String APP_VERSION = "app_version";
    public static final String SURVEY_ID = "survey_id";
    public static final String MULTIPLE_CHOICES_DELIMITER = ";";

    /**
     * Join event.
     */
    public static final String JOIN_EVENT_COLLECTION = "JOIN_EVENT_COLLECTION";
    public static final String PHONE_MAKE = "make";
    public static final String PHONE_MODEL = "model";
    public static final String ANDROID_VERSION = "android_version";
    public static final String CARRIER = "carrier";
    public static final String LOCALE = "locale";
    public static final String COUNTRY_CODE = "country_code";

    /**
     * App install survey.
     */
    public static final String WHY_INSTALL = "why_install";
    public static final String KNOW_PERMISSION_REQUIRED = "know_permission_required";
    public static final String INSTALL_FACTORS = "install_factors";
    public static final String PERMISSIONS_THINK_REQUIRED = "permission_think_required";
    public static final String EVENT_SERVER_ID = "event_server_id";

    /**
     * App uninstall survey.
     */
    public static final String WHY_UNINSTALL = "why_uninstall";
    public static final String PERMISSION_REMEMBERED_REQUESTED = "permission_remembered_requested";

    /**
     * Permission grant/deny survey.
     */
    public static final String WHY_GRANT = "why_grant";
    public static final String WHY_DENY = "why_deny";
    public static final String EXPECTED_PERMISSION_REQUEST = "expected_request";
    public static final String COMFORT_LEVEL = "comfort_level";

    /**
     * App install.
     */
    public static final String APP_INSTALL_COLLECTION = "APP_INSTALL_COLLECTION";
    public static final String APP_INSTALL_SURVEY_COLLECTION = "APP_INSTALL_SURVEY_COLLECTION";

    /**
     * App uninstall.
     */
    public static final String APP_UNINSTALL_COLLECTION = "APP_UNINSTALL_COLLECTION";
    public static final String APP_UNINSTALL_SURVEY_COLLECTION = "APP_UNINSTALL_SURVEY_COLLECTION";

    /**
     * Permission.
     */
    public static final String PERMISSION_COLLECTION = "PERMISSION_COLLECTION";
    public static final String PERMISSION_GRANT_SURVEY_COLLECTION = "PERMISSION_GRANT_SURVEY_COLLECTION";
    public static final String PERMISSION_DENY_SURVEY_COLLECTION = "PERMISSION_DENY_SURVEY_COLLECTION";
    public static final String PERMISSION_REQUESTED_NAME = "permission_requested";
    public static final String GRANTED = "granted";
    public static final String INITIATED_BY_USER = "user_initiated";

    /**
     * Proactive permission request event.
     */
    public static final String PROACTIVE_RATIONALE_COLLECTION = "PROACTIVE_RATIONALE_COLLECTION";
    public static final String PROACTIVE_RATIONALE_MESSAGE = "rationale_message";
    public static final String PROACTIVE_REQUEST_GRANTED = "granted";
    public static final String PROACTIVE_REQUEST_PERMISSION_EVENT_CORRELATION_ID = "event_correlation_id";

    /**
     * Demographic event.
     */
    public static final String DEMOGRAPHIC_COLLECTION = "DEMOGRAPHIC_COLLECTION";
    public static final String EDUCATION = "education";
    public static final String INCOME = "income";
    public static final String AGE = "age";
    public static final String GENDER = "gender";
    public static final String INDUSTRY = "industry";
    public static final String DAILY_USAGE = "daily_usage";
    public static final String STATUS = "status";
    public static final String COUNTRY = "country";

    /**
     * Rewards event.
     */
    public static final String REWARDS_COLLECTION = "REWARDS_COLLECTION";
    public static final String REWARDS_METHOD = "rewards_method";
    public static final String REWARDS_METHOD_VALUE = "method_value";
    public static final String REWARDS_JOIN_DATE = "join_date";

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
            case PERMISSION_EVENT_TYPE:
                return surveyedType == SURVEYED_EVENT ? serverId2permissionServerSurveyedEvents :
                        serverId2permissionServerUnsurveyedEvents;
            default:
                return null;
        }
    }
}

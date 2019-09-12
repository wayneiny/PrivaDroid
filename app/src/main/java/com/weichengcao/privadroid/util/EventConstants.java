package com.weichengcao.privadroid.util;

public class EventConstants {
    // common
    public static final String USER_AD_ID = "ad_id";
    public static final String LOGGED_TIME = "logged_time";
    public static final String APP_NAME = "app_name";
    public static final String PACKAGE_NAME = "package_name";
    public static final String APP_VERSION = "app_version";
    public static final String SURVEYED = "surveyed";
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
}
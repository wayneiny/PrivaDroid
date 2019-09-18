package com.weichengcao.privadroid.database;

public class BaseServerEvent {

    /**
     * Member variable names == EventUtil columns
     */
    private String ad_id;
    private String app_name;
    private String app_version;
    private String logged_time;
    private String package_name;
    private String surveyed;

    private String serverId;
    private int eventType;

    public BaseServerEvent(String serverId, String ad_id, String app_name, String app_version,
                           String logged_time, String package_name, String surveyed, int eventType) {
        this.ad_id = ad_id;
        this.app_name = app_name;
        this.app_version = app_version;
        this.logged_time = logged_time;
        this.package_name = package_name;
        this.surveyed = surveyed;
        this.eventType = eventType;
        this.serverId = serverId;
    }

    public boolean isEventSurveyed() {
        return Boolean.parseBoolean(surveyed);
    }

    public String getAd_id() {
        return ad_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public String getApp_version() {
        return app_version;
    }

    public String getLogged_time() {
        return logged_time;
    }

    public String getPackage_name() {
        return package_name;
    }

    public String getSurveyed() {
        return surveyed;
    }

    public int getEventType() {
        return eventType;
    }
}

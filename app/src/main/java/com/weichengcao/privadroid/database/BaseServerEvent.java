package com.weichengcao.privadroid.database;

public class BaseServerEvent {

    /**
     * Member variable names == EventUtil columns
     */
    private String adId;
    private String appName;
    private String appVersion;
    private String loggedTime;
    private String packageName;
    private String surveyed;

    private String serverId;
    private int eventType;

    public BaseServerEvent(String serverId, String adId, String appName, String appVersion,
                           String loggedTime, String packageName, String surveyed, int eventType) {
        this.adId = adId;
        this.appName = appName;
        this.appVersion = appVersion;
        this.loggedTime = loggedTime;
        this.packageName = packageName;
        this.surveyed = surveyed;
        this.eventType = eventType;
        this.serverId = serverId;
    }

    public boolean isEventSurveyed() {
        return Boolean.parseBoolean(surveyed);
    }

    public String getAdId() {
        return adId;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getLoggedTime() {
        return loggedTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSurveyed() {
        return surveyed;
    }

    public int getEventType() {
        return eventType;
    }
}

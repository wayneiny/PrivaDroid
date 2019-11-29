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
    private String surveyId;

    private String serverId;
    private int eventType;

    public BaseServerEvent(String serverId, String adId, String appName, String appVersion,
                           String loggedTime, String packageName, String surveyId, int eventType) {
        this.adId = adId;
        this.appName = appName;
        this.appVersion = appVersion;
        this.loggedTime = loggedTime;
        this.packageName = packageName;
        this.surveyId = surveyId;
        this.eventType = eventType;
        this.serverId = serverId;
    }

    public boolean isEventSurveyed() {
        return !surveyId.isEmpty();
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

    public String getSurveyId() {
        return surveyId;
    }

    public int getEventType() {
        return eventType;
    }

    public String getServerId() {
        return serverId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }
}

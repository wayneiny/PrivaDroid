package com.weichengcao.privadroid.database;

public class BaseServerSurvey {

    private String adId;
    private String loggedTime;
    private int eventType;
    private String eventServerId;
    private String serverId;

    public BaseServerSurvey(String adId, String loggedTime, int eventType, String eventServerId, String serverId) {
        this.adId = adId;
        this.loggedTime = loggedTime;
        this.eventType = eventType;
        this.eventServerId = eventServerId;
        this.serverId = serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public String getLoggedTime() {
        return loggedTime;
    }

    public String getAdId() {
        return adId;
    }

    public int getEventType() {
        return eventType;
    }

    public String getEventServerId() {
        return eventServerId;
    }
}

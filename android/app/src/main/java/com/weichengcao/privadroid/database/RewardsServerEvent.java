package com.weichengcao.privadroid.database;

public class RewardsServerEvent {

    private String adId;
    private String loggedTime;
    private String joinDate;
    private String methodName;
    private String methodValue;

    public RewardsServerEvent(String adId, String loggedTime, String joinDate, String methodName, String methodValue) {
        this.adId = adId;
        this.loggedTime = loggedTime;
        this.joinDate = joinDate;
        this.methodName = methodName;
        this.methodValue = methodValue;
    }

    public String getAdId() {
        return adId;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public String getLoggedTime() {
        return loggedTime;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodValue() {
        return methodValue;
    }
}

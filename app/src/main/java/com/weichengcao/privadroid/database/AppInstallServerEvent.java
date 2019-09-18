package com.weichengcao.privadroid.database;

public class AppInstallServerEvent extends BaseServerEvent {

    public AppInstallServerEvent(String serverId, String adId, String appName, String appVersion, String loggedTime,
                                 String packageName, String surveyed, int eventType) {
        super(serverId, adId, appName, appVersion, loggedTime, packageName, surveyed, eventType);
    }
}

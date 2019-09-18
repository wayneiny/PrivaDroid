package com.weichengcao.privadroid.database;

public class AppUninstallServerEvent extends BaseServerEvent {

    public AppUninstallServerEvent(String serverId, String adId, String appName, String appVersion, String loggedTime,
                                   String packageName, String surveyed, int eventType) {
        super(serverId, adId, appName, appVersion, loggedTime, packageName, surveyed, eventType);
    }
}

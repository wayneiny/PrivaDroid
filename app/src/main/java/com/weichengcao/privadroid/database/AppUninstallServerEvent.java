package com.weichengcao.privadroid.database;

public class AppUninstallServerEvent extends BaseServerEvent {

    public AppUninstallServerEvent(String appName, String packageName, String appVersion,
                                   String loggedTime, String surveyed, String adId) {
        super(adId, appName, appVersion, loggedTime, packageName, surveyed);
    }
}

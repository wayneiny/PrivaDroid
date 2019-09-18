package com.weichengcao.privadroid.database;

public class AppInstallServerEvent extends BaseServerEvent {

    public AppInstallServerEvent(String appName, String packageName, String appVersion,
                                 String loggedTime, String surveyed, String adId) {
        super(adId, appName, appVersion, loggedTime, packageName, surveyed);
    }
}

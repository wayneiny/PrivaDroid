package com.weichengcao.privadroid.database;

public class PermissionServerEvent extends BaseServerEvent {

    private String initiatedByUser;
    private String permissionName;
    private String requestGranted;

    public PermissionServerEvent(String serverId, String adId, String appName, String appVersion, String loggedTime,
                                 String packageName, String surveyId, int eventType,
                                 String initiatedByUser, String permissionName, String requestGranted) {
        super(serverId, adId, appName, appVersion, loggedTime, packageName, surveyId, eventType);

        this.initiatedByUser = initiatedByUser;
        this.permissionName = permissionName;
        this.requestGranted = requestGranted;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getInitiatedByUser() {
        return initiatedByUser;
    }

    public String getRequestGranted() {
        return requestGranted;
    }

    public boolean isRequestedGranted() {
        return Boolean.parseBoolean(requestGranted);
    }

    public boolean isInitiatedByUser() {
        return Boolean.parseBoolean(initiatedByUser);
    }
}

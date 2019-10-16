package com.weichengcao.privadroid.database;

import com.weichengcao.privadroid.sensors.QAccessibilityHandler;

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
        return requestGranted.equals(Boolean.toString(true)) || requestGranted.equals(QAccessibilityHandler.FOREGROUND_ONLY);
    }

    public boolean isInitiatedByUser() {
        return Boolean.parseBoolean(initiatedByUser);
    }

    public String getTypeOfGrantDeny() {
        if (requestGranted.equals(Boolean.toString(true))) {
            return ALWAYS_ALLOW;
        } else if (requestGranted.equals(QAccessibilityHandler.FOREGROUND_ONLY)) {
            return FOREGROUND_ALLOW;
        } else {
            return ALWAYS_DENY;
        }
    }

    public static final String ALWAYS_ALLOW = "ALWAYS_ALLOW";
    public static final String FOREGROUND_ALLOW = "FOREGROUND_ALLOW";
    public static final String ALWAYS_DENY = "ALWAYS_DENY";
}

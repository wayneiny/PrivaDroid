package com.weichengcao.privadroid.database;

import static com.weichengcao.privadroid.util.EventUtil.MULTIPLE_CHOICES_DELIMITER;

public class AppUninstallServerSurvey extends BaseServerSurvey {

    private String why;
    private String permissionsRequestedRemembered;

    public AppUninstallServerSurvey(String adId, String loggedTime, int eventType,
                                    String why, String permissionsRequestedRemembered,
                                    String eventServerId, String serverId) {
        super(adId, loggedTime, eventType, eventServerId, serverId);
        this.why = why;
        this.permissionsRequestedRemembered = permissionsRequestedRemembered;
    }

    public String getWhy() {
        return why;
    }

    // TODO: make it true multiple choice
    public String[] getPermissionsRequestedRemembered() {
        return permissionsRequestedRemembered.split(MULTIPLE_CHOICES_DELIMITER);
    }
}
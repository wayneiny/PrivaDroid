package com.weichengcao.privadroid.database;

import static com.weichengcao.privadroid.util.EventUtil.MULTIPLE_CHOICES_DELIMITER;

public class AppInstallServerSurvey extends BaseServerSurvey {

    private String why;
    private String factors;
    private String knowPermission;
    private String thinkPermissions;

    public AppInstallServerSurvey(String adId, String loggedTime, int eventType,
                                  String why, String factors, String knowPermission, String thinkPermissions,
                                  String eventServerId, String serverId) {
        super(adId, loggedTime, eventType, eventServerId, serverId);
        this.why = why;
        this.factors = factors;
        this.knowPermission = knowPermission;
        this.thinkPermissions = thinkPermissions;
    }

    public String getWhy() {
        return why;
    }

    public String getKnowPermission() {
        return knowPermission;
    }

    public String[] getFactors() {
        return factors.split(MULTIPLE_CHOICES_DELIMITER);
    }

    public String[] getThinkPermissions() {
        return thinkPermissions.split(MULTIPLE_CHOICES_DELIMITER);
    }
}

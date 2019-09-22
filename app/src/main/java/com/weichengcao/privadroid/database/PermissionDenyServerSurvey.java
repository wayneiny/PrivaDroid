package com.weichengcao.privadroid.database;

public class PermissionDenyServerSurvey extends BaseServerSurvey {

    private String whyDeny;
    private String expectedPermissionRequest;
    private String comfortableLevelDenying;

    public PermissionDenyServerSurvey(String adId, String loggedTime, int eventType, String eventServerId, String serverId,
                                      String whyDeny, String expectedPermissionRequest, String comfortableLevelDenying) {
        super(adId, loggedTime, eventType, eventServerId, serverId);
        this.whyDeny = whyDeny;
        this.expectedPermissionRequest = expectedPermissionRequest;
        this.comfortableLevelDenying = comfortableLevelDenying;
    }

    public String getWhyDeny() {
        return whyDeny;
    }

    public String getComfortableLevelDenying() {
        return comfortableLevelDenying;
    }

    public boolean ifExpectedThisPermissionRequest() {
        return Boolean.parseBoolean(expectedPermissionRequest);
    }

    public String getExpectedPermissionRequest() {
        return expectedPermissionRequest;
    }
}

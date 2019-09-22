package com.weichengcao.privadroid.database;

public class PermissionGrantServerSurvey extends BaseServerSurvey {

    private String whyGrant;
    private String expectedPermissionRequest;
    private String comfortableLevelGranting;

    public PermissionGrantServerSurvey(String adId, String loggedTime, int eventType, String eventServerId, String serverId,
                                       String whyGrant, String expectedPermissionRequest, String comfortableLevelGranting) {
        super(adId, loggedTime, eventType, eventServerId, serverId);
        this.whyGrant = whyGrant;
        this.expectedPermissionRequest = expectedPermissionRequest;
        this.comfortableLevelGranting = comfortableLevelGranting;
    }

    public String getWhyGrant() {
        return whyGrant;
    }

    public String getComfortableLevelGranting() {
        return comfortableLevelGranting;
    }

    public boolean ifExpectedThisPermissionRequest() {
        return Boolean.parseBoolean(expectedPermissionRequest);
    }

    public String getExpectedPermissionRequest() {
        return expectedPermissionRequest;
    }
}

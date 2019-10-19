package com.weichengcao.privadroid.database;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

public class PermissionGrantServerSurvey extends BaseServerSurvey {

    private String whyGrant;
    private String expectedPermissionRequest;
    private String comfortableLevelGranting;
    private String onlyGrantTemporarily;
    private String wouldLikeReminderNotification;

    public PermissionGrantServerSurvey(String adId, String loggedTime, int eventType, String eventServerId, String serverId,
                                       String whyGrant, String expectedPermissionRequest, String comfortableLevelGranting,
                                       String onlyGrantTemporarily, String wouldLikeReminderNotification) {
        super(adId, loggedTime, eventType, eventServerId, serverId);
        this.whyGrant = whyGrant;
        this.expectedPermissionRequest = expectedPermissionRequest;
        this.comfortableLevelGranting = comfortableLevelGranting;
        this.onlyGrantTemporarily = onlyGrantTemporarily;
        this.wouldLikeReminderNotification = wouldLikeReminderNotification;
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

    public boolean getOnlyGrantTemporarily() {
        return onlyGrantTemporarily.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.yes));
    }

    public String getOnlyGrantTemporarilyText() {
        return onlyGrantTemporarily;
    }

    public boolean getWouldLikeReminderNotification() {
        return wouldLikeReminderNotification.equalsIgnoreCase(PrivaDroidApplication.getAppContext().getString(R.string.yes));
    }

    public String getWouldLikeReminderNotificationText() {
        return wouldLikeReminderNotification;
    }
}

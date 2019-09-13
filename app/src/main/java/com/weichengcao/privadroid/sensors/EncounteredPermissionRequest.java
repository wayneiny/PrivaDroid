package com.weichengcao.privadroid.sensors;

import com.weichengcao.privadroid.util.DatetimeUtil;

/**
 * Class used by AccessibilityEventMonitorService to log a permission request. Used for tying requests to their
 * responses.
 */
public class EncounteredPermissionRequest {
    /**
     * The requested permission (we might change this to be one of several constants used in the
     * Android system in the future)
     */
    private String permissionString;

    /**
     * The time (in milliseconds since epoch) at which the request happened
     */
    private String timestamp;

    /**
     * The name of the application if known (null otherwise)
     */
    private String appName;

    public EncounteredPermissionRequest(String permissionString, String appName) {
        this.permissionString = permissionString;
        this.timestamp = DatetimeUtil.getCurrentIsoDatetime();
        this.appName = appName;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAppName() {
        return appName;
    }
}

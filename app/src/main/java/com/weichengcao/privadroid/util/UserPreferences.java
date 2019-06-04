package com.weichengcao.privadroid.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    private static final String APP_PREFERENCES = "PrivaDroidPreferences";

    private Context context;
    private SharedPreferences prefs;

    public UserPreferences(Context context) {
        this.context = context;
    }

    SharedPreferences getAppPrefs() {
        if (prefs == null) {
            prefs = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        }
        return prefs;
    }

    // Mobile Ad Id [START]
    private static final String ADVERTISING_ID = "ADVERTISING_ID";

    public String getAdvertisingId() {
        return getAppPrefs().getString(ADVERTISING_ID, "");
    }

    public void setAdvertisingId(String advertisingId) {
        getAppPrefs().edit().putString(ADVERTISING_ID, advertisingId).apply();
    }
    // Mobile Ad Id [END]
}

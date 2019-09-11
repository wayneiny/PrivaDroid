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

    private SharedPreferences getAppPrefs() {
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

    // Consent [START]
    private static final String CONSENT_GRANTED = "CONSENT_GRANTED";

    public boolean getConsentGranted() {
        return getAppPrefs().getBoolean(CONSENT_GRANTED, false);
    }

    public void setConsentGranted(boolean consentGranted) {
        getAppPrefs().edit().putBoolean(CONSENT_GRANTED, consentGranted).apply();
    }
    // Consent [END]

    // Join event id in Firestore [Start]
    private static final String FIRESTORE_JOIN_EVENT_ID = "FIRESTORE_JOIN_EVENT_ID";

    public String getFirestoreJoinEventId() {
        return getAppPrefs().getString(FIRESTORE_JOIN_EVENT_ID, "");
    }

    public void setFirestoreJoinEventId(String firestoreJoinEventId) {
        getAppPrefs().edit().putString(FIRESTORE_JOIN_EVENT_ID, firestoreJoinEventId).apply();
    }
    // Join event id in Firestore [End]
}

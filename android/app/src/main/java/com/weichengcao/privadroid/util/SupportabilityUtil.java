package com.weichengcao.privadroid.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.weichengcao.privadroid.PrivaDroidApplication;

import java.util.Locale;

public class SupportabilityUtil {

    public static boolean isInSupportedLanguages() {
        String deviceLanguage = Locale.getDefault().getISO3Language();
        Locale SPAIN = new Locale("es", "ES");
        return deviceLanguage.equals(Locale.ENGLISH.getISO3Language()) &&
                deviceLanguage.equals(Locale.FRANCE.getISO3Language()) &&
                deviceLanguage.equals(Locale.TRADITIONAL_CHINESE.getISO3Language()) &&
                deviceLanguage.equals(Locale.KOREA.getISO3Language()) &&
                deviceLanguage.equals(SPAIN.getISO3Language());
    }

    public static String getDeviceCountryCode() {
        TelephonyManager tm = (TelephonyManager) PrivaDroidApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = null;
        if (tm != null) {
            countryCode = tm.getNetworkCountryIso();
        }
        return countryCode;
    }
}

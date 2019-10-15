package com.weichengcao.privadroid.demographic;

import java.util.HashMap;

public class DemographicUtil {

    public static HashMap<String, String> countryCode2CountryNames = new HashMap<>();

    // The country codes are taken from https://www.mcc-mnc.com/.
    static {
        countryCode2CountryNames.put("ca", "canada");
        countryCode2CountryNames.put("us", "united_states");
        countryCode2CountryNames.put("sg", "singapore");
        countryCode2CountryNames.put("fr", "france");
        countryCode2CountryNames.put("es", "spain");
        countryCode2CountryNames.put("hk", "china");
        countryCode2CountryNames.put("gb", "united_kingdom");
        countryCode2CountryNames.put("za", "south_africa");
        countryCode2CountryNames.put("ar", "argentina");
        countryCode2CountryNames.put("in", "india");
        countryCode2CountryNames.put("kr", "south_korea");
    }

    public static String formatUserBaseStatFirebaseKey(String countryNameFromMap) {
        return String.format("%s_user_base", countryNameFromMap);
    }
}

package com.weichengcao.privadroid.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class DatetimeUtil {

    /**
     * Local time with timezone information.
     */
    public static String getCurrentIsoDatetime() {
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.print(dt);
    }

    public static String convertIsoToReadableFormat(String iso) {
        DateTime parsedDate = new DateTime(iso);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy/MM/dd");
        return dtfOut.print(parsedDate);
    }
}

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

    /**
     * Get e.g. 2019/09/15
     */
    public static String convertIsoToReadableFormat(String iso) {
        DateTime parsedDate = new DateTime(iso);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy/MM/dd");
        return dtfOut.print(parsedDate);
    }

    public static String convertIsoToReadableDatetimeFormat(String iso) {
        DateTime parsedDate = new DateTime(iso);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
        return dtfOut.print(parsedDate);
    }

    /**
     * Compare 2 iso date formats.
     */
    public static boolean aLaterThanBIso(String a, String b) {
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
        DateTime A = parser.parseDateTime(a);
        DateTime B = parser.parseDateTime(b);

        return A.isAfter(B);
    }

    /**
     * Hash the iso format date.
     */
    public static int getIsoHash(String iso) {
        DateTime parsedDate = new DateTime(iso);
        return parsedDate.hashCode();
    }
}

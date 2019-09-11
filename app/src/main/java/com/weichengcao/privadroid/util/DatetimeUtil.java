package com.weichengcao.privadroid.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class DatetimeUtil {

    /**
     * Local time with timezone information.
     *
     * @return
     */
    public static String getCurrentIsoDatetime() {
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.print(dt);
    }
}

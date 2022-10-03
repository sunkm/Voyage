package com.manchuan.tools.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Felix.Liang
 */
public class TimeFormatter {

    private static final SimpleDateFormat sFormatter;
    private static final SimpleDateFormat sFormatter2;

    static {
        sFormatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        sFormatter.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        sFormatter2 = new SimpleDateFormat("HH:mm:ss.SS", Locale.ENGLISH);
        sFormatter2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

    private static final Date sDate = new Date();

    public static String getFormatTime(long millis) {
        sDate.setTime(millis);
        return sFormatter.format(sDate);
    }

    public static String getFormatStopwatchTime(long millis) {
        sDate.setTime(millis);
        return sFormatter2.format(sDate);
    }
}


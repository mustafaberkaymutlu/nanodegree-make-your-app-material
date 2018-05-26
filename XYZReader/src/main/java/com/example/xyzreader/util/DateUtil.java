package com.example.xyzreader.util;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public final class DateUtil {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");

    // Use default locale format
    public static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat();

    // Most time functions can only handle 1902 - 2037
    public static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private DateUtil() {
        throw new AssertionError("No instances for you!");
    }

}

package com.example.xyzreader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;

public final class DateUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");

    // Use default locale format
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat();

    // Most time functions can only handle 1902 - 2037
    private static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private DateUtil() {
        throw new AssertionError("No instances for you!");
    }

    public static Date parsePublishedDate(String date) {
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException ex) {
            Timber.e(ex);
            Timber.i("passing today's date");
            return new Date();
        }
    }

    public static String formatOutput(Date publishedDate) {
        return OUTPUT_FORMAT.format(publishedDate);
    }

    public static boolean isBefore1902(Date date) {
        return date.before(START_OF_EPOCH.getTime());
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 *
 * @author thangpham
 */
public class DateTimeUtil {

    public static final int DASH = 0, SPLASH = 1, COLON = 2;
    public static final int YYYYMMDDHH = 1000;

    private static final FastDateFormat ISOFORMAT_UTC = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone("UTC"));
    
    private static final FastDateFormat YYYYMMDDHH_WITHDASH_UTC = FastDateFormat.getInstance("yyyy-MM-dd-HH", TimeZone.getTimeZone("UTC"));
    private static final FastDateFormat YYYYMMDDHH_WITHSPLASH_UTC = FastDateFormat.getInstance("yyyy/MM/dd/HH", TimeZone.getTimeZone("UTC"));
    private static final FastDateFormat YYYYMMDDHH_WITHCOLON_UTC = FastDateFormat.getInstance("yyyy:MM:dd:HH", TimeZone.getTimeZone("UTC"));

    private static final FastDateFormat YYYYMMDDHH_WITHDASH = FastDateFormat.getInstance("yyyy-MM-dd-HH");
    private static final FastDateFormat YYYYMMDDHH_WITHSPLASH = FastDateFormat.getInstance("yyyy/MM/dd/HH");
    private static final FastDateFormat YYYYMMDDHH_WITHCOLON = FastDateFormat.getInstance("yyyy:MM:dd:HH");
    
    public static String formatISOTime(Calendar cal) {
        return ISOFORMAT_UTC.format(cal);
    }
    
    public static String formatISOTime(Date date) {
        return ISOFORMAT_UTC.format(date);
    }
    
    public static String formatISOTime(long timeInMilis) {
        return ISOFORMAT_UTC.format(timeInMilis);
    }
    
    public static Date parseISOTime(String timeStr) throws ParseException {
      return ISOFORMAT_UTC.parse(timeStr);
    }

    public static Date parseUTCTime(String timeStr, int arrangement, int symbol) throws ParseException {
        Date result;
        if (YYYYMMDDHH == arrangement) {
            switch (symbol) {
                case DASH:
                    result = YYYYMMDDHH_WITHDASH_UTC.parse(timeStr);
                    break;
                case SPLASH:
                    result = YYYYMMDDHH_WITHSPLASH_UTC.parse(timeStr);
                    break;
                default:
                    result = YYYYMMDDHH_WITHCOLON_UTC.parse(timeStr);
                    break;
            }
        } else {
            throw new IllegalArgumentException("Time arrangement or symbol is wrong.");
        }
        return result;
    }

    public static Date formatSystemTime(String timeStr, int arrangement, int symbol) throws ParseException {
        Date result;
        if (YYYYMMDDHH == arrangement) {
            switch (symbol) {
                case DASH:
                    result = YYYYMMDDHH_WITHDASH.parse(timeStr);
                    break;
                case SPLASH:
                    result = YYYYMMDDHH_WITHSPLASH.parse(timeStr);
                    break;
                default:
                    result = YYYYMMDDHH_WITHCOLON.parse(timeStr);
                    break;
            }
        } else {
            throw new IllegalArgumentException("Time arrangement or symbol is wrong.");
        }
        return result;
    }

    public static String formatSystemTime(Calendar cal, int arrangement, int symbol) {
        return formatSystemTime(cal.getTimeInMillis(), arrangement, symbol);
    }

    public static String formatSystemTime(Date dateTime, int arrangement, int symbol) {
        return formatSystemTime(dateTime.getTime(), arrangement, symbol);
    }

    public static String formatSystemTime(long timeInMilis, int arrangement, int symbol) {
        String result;
        if (YYYYMMDDHH == arrangement) {
            switch (symbol) {
                case DASH:
                    result = YYYYMMDDHH_WITHDASH.format(timeInMilis);
                    break;
                case SPLASH:
                    result = YYYYMMDDHH_WITHSPLASH.format(timeInMilis);
                    break;
                default:
                    result = YYYYMMDDHH_WITHCOLON.format(timeInMilis);
                    break;
            }
        } else {
            throw new IllegalArgumentException("Time arrangement or symbol is wrong.");
        }
        return result;
    }

    public static String formatTimeUTC(Date dateTime, int arrangement, int symbol) {
        return formatTimeUTC(dateTime.getTime(), arrangement, symbol);
    }

    public static String formatTimeUTC(Calendar cal, int arrangement, int symbol) {
        return formatTimeUTC(cal.getTimeInMillis(), arrangement, symbol);
    }

    public static String formatTimeUTC(long timeInMilis, int arrangement, int symbol) {
        String result;
        if (YYYYMMDDHH == arrangement) {
            switch (symbol) {
                case DASH:
                    result = YYYYMMDDHH_WITHDASH_UTC.format(timeInMilis);
                    break;
                case SPLASH:
                    result = YYYYMMDDHH_WITHSPLASH_UTC.format(timeInMilis);
                    break;
                default:
                    result = YYYYMMDDHH_WITHCOLON_UTC.format(timeInMilis);
                    break;
            }
        } else {
            throw new IllegalArgumentException("Time arrangement or symbol is wrong.");
        }
        return result;
    }

}

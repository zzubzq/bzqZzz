package com.bzq.bzqZzz.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zzubzq on 2018/8/31.
 */
public class DateUtil {
    /**
     * 缺省的日期显示格式： yyyy-MM-dd
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * yyyy-MM
     */
    public static final String DEFAULT_DATE_FORMAT_YYYY_MM = "yyyy-MM";
    /**
     * 缺省的日期时间显示格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 一天开始时间尾数
     */
    public static final String BEGIN_TIME = " 00:00:00";
    /**
     * 一天结束时间尾数
     */
    public static final String END_TIME = " 23:59:59";

    private static ThreadLocal<SimpleDateFormat> defaultDateFormat = ThreadLocal.withInitial(() ->
            new SimpleDateFormat(DEFAULT_DATE_FORMAT)
    );

    private static ThreadLocal<SimpleDateFormat> defaultDateTimeFormat = ThreadLocal.withInitial(() ->
            new SimpleDateFormat(DEFAULT_DATETIME_FORMAT)
    );

    private static ThreadLocal<SimpleDateFormat> defaultTimeFormat = ThreadLocal.withInitial(() ->
            new SimpleDateFormat(DEFAULT_TIME_FORMAT)
    );

    /**
     * 从datetime格式的字符串获取date字符串
     * eg getDateStrFromDateTimeStr("2012-11-11 11:11:11")
     * return "2012-11-11"
     */
    public static String getDateStrFromDateTimeStr(String dateTimeStr) {
        return dateTimeStr.substring(0, 10);
    }

    public static Date getDateFromDateTimeStr(String dateTimeStr) {
        SimpleDateFormat sdf = defaultDateTimeFormat.get();
        Date date = null;
        try {
            date = sdf.parse(dateTimeStr);
        } catch (ParseException e) {
        }
        return date;
    }

    public static Date getDateFromTimeStr(String dateTimeStr) {
        SimpleDateFormat sdf = defaultTimeFormat.get();
        Date date = null;
        try {
            date = sdf.parse(dateTimeStr);
        } catch (ParseException e) {
        }
        return date;
    }

    /**
     * 获取日期的开始时间
     * eg: Date date =  getDateByString("2013-11-11","yyyy-MM-dd")
     * getDateTimeBegin(date)
     * return   "2013-11-11 00:00:00"
     *
     * @param date 日期
     */
    public static Date getDateTimeBegin(Date date) {
        if (date == null) {
            return null;
        }
        String dateStr = getDateString(date) + BEGIN_TIME;
        return getDateByString(dateStr, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 获取日期的结束时间
     *
     * @param date 日期
     */
    public static Date getDateTimeEnd(Date date) {
        if (date == null) {
            return null;
        }
        String dateStr = getDateString(date) + END_TIME;
        return getDateByString(dateStr, DEFAULT_DATETIME_FORMAT);
    }


    /**
     * 获取今天的开始时间
     */
    public static Date getCurrentDayDateTimeBegin() {
        return getDateTimeBegin(new Date());
    }

    /**
     * 获取今天的结束时间
     */
    public static Date getCurrentDayDateTimeEnd() {
        return getDateTimeEnd(new Date());
    }

    /**
     * 获取本周的开始时间
     */
    public static Date getCurrentWeekDateTimeBegin() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -6);
        } else {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        return getDateTimeBegin(calendar.getTime());
    }

    /**
     * 获取本周的结束时间
     */
    public static Date getCurrentWeekDateTimeEnd() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek > Calendar.SUNDAY) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.add(Calendar.DAY_OF_MONTH, 6);
        }
        return getDateTimeEnd(calendar.getTime());
    }

    /**
     * 获取本月的开始时间
     */
    public static Date getCurrentMonthDateTimeBegin() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getDateTimeBegin(calendar.getTime());
    }

    /**
     * 获取本月的结束时间
     */
    public static Date getCurrentMonthDateTimeEnd() {
        Calendar calendar = Calendar.getInstance();
        //将日期设置为下一月第一天
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1);
        //减去1天，得到的即本月的最后一天
        calendar.add(Calendar.DATE, -1);
        return getDateTimeEnd(calendar.getTime());
    }

    /**
     * 得到日期的字符串
     *
     * @param date   java.util.Date
     * @param format eg.yyyy-mm-dd
     */
    public static String getDateString(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 得到日期的字符串
     *
     * @param date java.util.Date
     * @return "yyyy-MM-dd";
     */
    public static String getDateString(Date date) {
        SimpleDateFormat dateFormat = defaultDateFormat.get();
        return dateFormat.format(date);
    }

    /**
     * 获得日期时间字符串
     */
    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = defaultDateTimeFormat.get();
        return dateFormat.format(date);
    }

    /**
     * 根据日期字符串获取日期对象
     *
     * @param dateStr eg."2013-11-12 12:11:22"
     * @param format  eg."yyyy-MM-dd HH:mm:ss"
     */
    public static Date getDateByString(String dateStr, String format) {
        if (dateStr == null || "".equals(dateStr.trim())) {
            return null;
        }
        Date parse = null;
        try {
            parse = new SimpleDateFormat(format).parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Covert error data:" + dateStr, e);
        }
        return parse;
    }

    /**
     * 获取两个日期之之间的天数（保证两个日期的时分秒是一致的）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数
     */
    public static int getDays(Date startDate, Date endDate) {
        return (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24)) + 1;
    }
}

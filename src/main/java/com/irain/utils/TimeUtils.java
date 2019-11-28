package com.irain.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author: w
 * @Date: 2019/11/15 9:24 上午
 * 日期工具类
 */
public class TimeUtils {

    /**
     * 字符串类型的time转换为Date类型
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static Date timeStrToDate(String dateStr, String format) throws ParseException {
        if (dateStr.length() > 0) {
            DateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(dateStr);
        }
        return null;
    }

    /**
     * 获取当前系统时间
     *
     * @return
     */
    public static String getNowTimeStr() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    /**
     * 获取昨天的日期
     *
     * @return
     */
    public static String getYesterDayStr() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return new SimpleDateFormat("yyyyMMdd ").format(cal.getTime());
    }
}
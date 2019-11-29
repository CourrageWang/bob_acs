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

    /**
     * 比较两个时间相差的秒数
     *
     * @param time1
     * @param time2
     * @return
     */
    public static long getSecondsBetweenTime(String time1, String time2) {
        long diffSeconds = 0;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Date parse = format.parse(time1);
            Date date = format.parse(time2);
            long between = Math.abs(date.getTime() - parse.getTime());
            long day = between / (24 * 60 * 60 * 1000);
            long hour = (between / (60 * 60 * 1000) - day * 24);
            long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long seconds = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
            diffSeconds = day * 24 * 60 * 60 + hour * 60 * 60 + min * 60 + seconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diffSeconds;
    }

    /**
     * 获取当前的时间按照指定的格式
     *
     * @param format
     * @return
     */
    public static String getStrNowtimeWithformat(String format) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    /**
     * 根据从控制器返回的数据解析出想要的数据
     *
     * @param time
     * @return
     */
    public static String getDeviceTimeByInput(String time) {
        String str = time.substring(3, time.length() - 7);
        //只去偶数位的数据拼接
        char[] chars = str.toCharArray();
        int charLen = chars.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charLen; i++) {
            if (i % 2 == 1) {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
}
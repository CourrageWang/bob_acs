package com.irain.utils;

import lombok.extern.log4j.Log4j;

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
@Log4j
public class TimeUtils {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    private static final DateFormat DAY_FORMAT = new SimpleDateFormat("yy-MM-dd");

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
        return new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
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

    /**
     * 获取给定时间对应的毫秒数
     *
     * @param time "HH:mm:ss"
     * @return
     */
    public static long getTimeMillis(String time) {
        try {
            Date currentDate = DATE_FORMAT.parse(DAY_FORMAT.format(new Date()) + " " + time);
            return currentDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 比较两个时间 当前一个时间大于后一个时间则返回1 ，小于返回-1 相等返回0
     *
     * @param Date1
     * @param Date2
     * @param format
     * @return
     */
    public static int compareDate(String Date1, String Date2, String format) {
        DateFormat df = new SimpleDateFormat(format);
        try {
            Date date1 = df.parse(Date1);
            Date date2 = df.parse(Date2);
            return date1.compareTo(date2);
        } catch (Exception e) {
            log.error("日期格式数据存在异常" + e.getMessage());
        }
        return 0;
    }

    /**
     * 获取当前月份的季度数
     *
     * @return
     */
    public static String getSeason() {
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH) + 1;
        if (month == 1 | month == 2 || month == 3) {
            return "one";
        } else if (month == 4 || month == 5 || month == 6) {
            return "two";
        } else if (month == 7 || month == 8 || month == 9) {
            return "three";
        } else if (month == 10 || month == 11 || month == 12) {
            return "four";
        }
        return "";
    }

    /**
     * 检验时间是否合法
     *
     * @param str     原始数据
     * @param formats 日期格式
     * @return
     */
    public static boolean isValidDate(String str, String formats) {
        boolean convertSuccess = true;// 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        SimpleDateFormat sdf = new SimpleDateFormat(formats);
        try {// 设置lenient为false.否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            sdf.setLenient(false);
            sdf.parse(str);
        } catch (ParseException e) { // e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /**
     * 获取年份已经当前的季度数
     *
     * @return 2019-1 // 2019年一季度
     */
    public static String getYearWithSeason() {
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH) + 1;
        String year = String.valueOf(now.get(Calendar.YEAR));

        // 新一年的第一天仍然导出的上一年的最后一天的数据
        // 也就是2020年1月1日实际上是2020年的第一季度，但是由于这一天导出的数据是2019年12月31号
        // 的数据仍然要按照2019-4 文件夹下 因此需要特殊处理

        if (month == 1 | month == 2 || month == 3) {
            if (month == 1 && now.get(Calendar.DATE) == 1) {
                Integer ye = Integer.parseInt(year) - 1;
                return ye + "-" + "4";
            }
            return year + "-" + "1";
        } else if (month == 4 || month == 5 || month == 6) {
            if (month == 4 && now.get(Calendar.DATE) == 1) {
                return year + "-" + "1";
            }
            return year + "-" + "2";
        } else if (month == 7 || month == 8 || month == 9) {
            if (month == 7 && now.get(Calendar.DATE) == 1) {
                return year + "-" + "2";
            }
            return year + "-" + "3";
        } else if (month == 10 || month == 11 || month == 12) {

            if (month == 10 && now.get(Calendar.DATE) == 1) {
                return year + "-" + "3";
            }
            return year + "-" + "4";
        }
        return "";
    }
}
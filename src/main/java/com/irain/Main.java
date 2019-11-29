package com.irain;

import com.irain.task.TimeTask;

/**
 * @Author: w
 * @Date: 2019/11/14 3:48 下午
 * 启动类 同步数据与设备状态检测使用定时任务完成;
 */

public class Main {
    public static void main(String[] args) {
        //每天的凌晨1点开始执行定时任务完成前一日考勤数据导入;
        new TimeTask().dayOfLoadData("19:17:40");
        //每一个小时监测所有设备连接是否正常并同时执行校时操作【每小时执行一次监测程序】；
//        new TimeTask().checkConnection();
        //每天凌晨四点开始将所有打卡机上的数据同步，并通过增量的方式存储在指定的excel文件夹下；

    }
}
package com.irain;

import com.irain.task.TimeTask;

/**
 * @Author: w
 * @Date: 2019/11/14 3:48 下午
 * 启动类 同步数据与设备状态检测使用定时任务完成;
 */

public class Main {
    public static void main(String[] args) {
        //每天的凌晨1点开始执行定时任务完成前一日数据导入;
        new TimeTask().dayOfLoadData("01:00:00");
        //每一个小时监测设备连接是否正常【每小时执行一次监测程序】
        new TimeTask().checkConnection();
    }
}
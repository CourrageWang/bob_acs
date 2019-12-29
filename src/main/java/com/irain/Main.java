package com.irain;

import com.irain.conf.LoadConf;
import com.irain.task.TimeTask;
import lombok.extern.log4j.Log4j;

/**
 * @Author: w
 * @Date: 2019/11/14 3:48 下午
 * 启动类 同步数据与设备状态检测使用定时任务完成;
 */
@Log4j
public class Main {

    public static void main(String[] args) {
        log.info("---------------程序开始执行-----------------");
        //加载配置文件
        new LoadConf();
        //每天的凌晨1点开始执行定时任务完成前一日考勤数据导入;
        new TimeTask().dayOfLoadSignData("01:00:10");

        //每一个小时监测所有设备连接是否正常并同时执行校时操作【每小时执行一次监测程序】；
        new TimeTask().checkConnection();

        //每天凌晨四点开始将所有打卡机上的数据同步，并通过增量的方式存储在指定的excel文件夹下；
        new TimeTask().dayOfLoadAllDeviceData("03:00:00");

    }
}
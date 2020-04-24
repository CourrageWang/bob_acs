package com.irain;

import com.irain.conf.LoadConf;
import com.irain.handle.InfoExection;
import com.irain.utils.TimeUtils;
import lombok.extern.log4j.Log4j;

/**
 * @Author: w
 * @Date: 2019/11/14 3:48 下午
 * 启动类 同步数据与设备状态检测使用定时任务完成;
 */
@Log4j
public class Main {

    public static void main(String[] args) {

        String loadData = "";
        if (args.length > 0) {
            loadData = args[1];
            if (!TimeUtils.isValidDate(loadData, "YYYYMMdd") || loadData.length() != 8) {
                log.info("******************************");
                log.info("*您输入的时间格式错误，请重新输入*");
                log.info("*  输入格式形如20200412的类型   *");
                log.info("*******************************");
            } else {
                log.info("数据格式正确程序即将录入" + loadData + "的数据！！");
                log.info("---------------程序开始执行-----------------");
                //加载配置文件
                new LoadConf();
                log.info("******获取考勤数据定时任务开始执行******");
                //开始处理输入数据
                InfoExection.execute(LoadConf.importDevicesMap, TimeUtils.getYesterDayStr().trim());
                log.info("******获取考勤数据定时任务执行结束*******");
            }
        } else {
            System.out.println("您输入的参数为空 请重新输入！！！");
        }
    }
}
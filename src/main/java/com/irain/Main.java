package com.irain;

import com.irain.conf.LoadConf;
import com.irain.handle.DeviceInfo;
import com.irain.handle.InfoExection;
import com.irain.utils.NetUtil;
import com.irain.utils.TimeUtils;
import lombok.extern.log4j.Log4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: w
 * @Date: 2019/11/14 3:48 下午
 */
@Log4j
public class Main {
    /**
     * 用户输入Ip地址和时间来导入打卡明细
     * java -jar -ip 127.0.0.1 -t 20200806
     *
     * @param args
     */
    public static void main(String[] args) {

        Map<String, String> devicesMap = new HashMap<>();// 存错所有设备信息；
        String loadData = "";
        String ipAddress = "";
        if (args.length > 0) {
            loadData = args[3];
            ipAddress = args[1];
            log.info(String.format("您输需要导入的是设备:%s 时间为%s的打卡数据", ipAddress, loadData));
            if (!TimeUtils.isValidDate(loadData, "YYYYMMdd") || loadData.length() != 8) {
                if (!NetUtil.ipCheck(ipAddress)) {
                    log.warn("您输入的ip地址" + ipAddress + "有误");
                    return;
                }
                log.info("******************************");
                log.info("*您输入的时间格式错误，请重新输入*");
                log.info("*  输入格式形如20200412的类型   *");
                log.info("*******************************");
                return;
            } else {
                log.info("数据格式正确程序即将录入设备" + ipAddress + loadData + "的打卡数据！！");
                log.info("---------------程序开始执行-----------------");
                //加载配置文件
                new LoadConf();
                //开始处理输入数据 日期为需要导入数据的日期
                // 如导入20200701的数据 则输入日需要重新导入的日期。
                // 将Ip地址塞入Map
                devicesMap.put(ipAddress, ipAddress);
                new DeviceInfo().loadAllDeviceData(devicesMap, loadData);
                log.info("******获取考勤数据定时任务执行结束*******");
            }
        } else {
            System.out.println("您输入的参数为空 请重新输入！！！");
        }
    }
}
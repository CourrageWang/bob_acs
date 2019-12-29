package com.irain.conf;

import com.irain.swing.ErrorDialog;
import com.irain.utils.FileUtils;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: w
 * @Date: 2019/11/21 4:11 下午
 * 加载配置文件
 */
@Log4j
@NoArgsConstructor
public class LoadConf {

    public static Map<String, String> propertiesMap = new HashMap<>(); //存储基本配置文件；
    public static Map<String, String> devicesMap = new HashMap<>();// 存储所有设备信息；
    public static Map<String, String> importDevicesMap = new HashMap<>();// 存错所有设备信息；
    private static final String PROPERTY_PATH = "D:\\access\\conf\\application.properties";
    public static Frame jFrame = ErrorDialog.frame;

    static {
        try {
            log.info(String.format("开始加载数据:%s", PROPERTY_PATH));
            propertiesMap = FileUtils.getProperties(PROPERTY_PATH);
            if (propertiesMap.size() == 0) {
                log.error("基础配置文件为空");
                System.exit(1);
            }
            log.info("加载基础配置完成");
            devicesMap = FileUtils.getProperties(propertiesMap.get("ALL_DEVICE"));
            if (devicesMap.size() == 0) {
                log.error("设备配置文件为空");
                System.exit(1);
            }
            log.info("加载所有设备数据成功");
            importDevicesMap = FileUtils.getProperties(propertiesMap.get("SIGN_DEVICE"));
            if (importDevicesMap.size() == 0) {
                log.error("打卡设备数据为空");
                System.exit(1);
            }
            log.info("加载打卡设备数据成功");

        } catch (Exception e) {
            log.error("导入配置文件发生异常:" + e.getMessage());
            System.exit(1);
        }
    }
}
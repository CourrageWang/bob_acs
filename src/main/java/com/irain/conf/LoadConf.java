package com.irain.conf;

import com.irain.utils.FileUtils;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: w
 * @Date: 2019/11/21 4:11 下午
 * 加载配置文件
 */
@Log4j
@NoArgsConstructor
public class LoadConf {

    public static Map<String, String> propertiesMap = new LinkedHashMap<>(); //存储基本配置文件；
    public static Map<String, String> devicesMap = new LinkedHashMap<>();// 存错所有设备信息；
    public static Map<String, String> importDevicesMap = new LinkedHashMap<>();// 存错所有设备信息；
    private static final String PROPERTY_PATH = "D:\\tools\\bob_acs\\src\\main\\resources\\application.properties";


    static {
        try {
            log.info(String.format("start to load conf at:%s", PROPERTY_PATH));
            propertiesMap = FileUtils.getProperties(PROPERTY_PATH);
            if (propertiesMap.size() == 0) {
                log.error("property file is empty please confirm");
                System.exit(1);
            }
            log.info("load base conf success");
            devicesMap = FileUtils.getProperties(propertiesMap.get("ALL_DEVICE"));
            if (devicesMap.size() == 0) {
                log.error("property file is empty please confirm");
                System.exit(1);
            }
            log.info("load device conf success");
            importDevicesMap = FileUtils.getProperties(propertiesMap.get("SIGN_DEVICE"));
            if (importDevicesMap.size() == 0) {
                log.error("property file is empty please confirm");
                System.exit(1);
            }
            log.info("load sign device conf success");

        } catch (Exception e) {
            log.error("load config error:" + e.getMessage());
            System.exit(1);
        }
    }
}
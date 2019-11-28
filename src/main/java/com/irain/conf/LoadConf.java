package com.irain.conf;

import com.irain.utils.FileUtils;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

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

    public static Map<String, String> propertiesMap = new HashMap<>(); //存储配置文件
    private static final String PROPERTY_PATH = "/Users/yqwang/Workspace/work/Codes/JavaCodes/comirain/src/main/resources/application.properties";

    static {
        try {
            log.info(String.format("start to load conf at:%s", PROPERTY_PATH));
            propertiesMap = FileUtils.getProperties(PROPERTY_PATH);
            if (propertiesMap.size() == 0) {
                log.error("property file is empty please confirm");
                System.exit(1);
            }
            log.info("load conf success!");
        } catch (Exception e) {
            log.error("load config error:" + e.getMessage());
            System.exit(1);
        }
    }
}
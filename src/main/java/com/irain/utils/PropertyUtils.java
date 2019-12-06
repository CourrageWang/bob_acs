package com.irain.utils;

import lombok.extern.log4j.Log4j;

import java.io.*;
import java.util.Properties;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/2 19:52
 * 用来读取配置文件属性
 **/
@Log4j
public class PropertyUtils {
    //属性文件的路径
    private String path;
    Properties props = new Properties();

    /**
     * 初始化配置文件
     *
     * @param path
     */
    public PropertyUtils(String path) {
        try {
            props.load(new FileInputStream(path));
        } catch (IOException e) {
            log.error("读取配置文件失败！！" + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 读取属性文件中相应键的值
     *
     * @param key 主键
     * @return String
     */
    public String getKeyValue(String key) {
        return props.getProperty(key);
    }

    /**
     * 根据主键key读取主键的值value
     *
     * @param filePath 属性文件路径
     * @param key      键名
     */
    public static String readValue(String filePath, String key) {
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(
                    filePath));
            props.load(in);
            String value = props.getProperty(key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新（或插入）一对properties信息(主键及其键值)
     * 如果该主键已经存在，更新该主键的值；
     * 如果该主键不存在，则插件一对键值。
     *
     * @param keyname  键名
     * @param keyvalue 键值
     */
    public void writeProperties(String keyname, String keyvalue, String path) {
        try {
            // 调用 Hashtable 的方法 put，使用 getProperty 方法提供并行性。
            // 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
            OutputStream fos = new FileOutputStream(path);
            props.setProperty(keyname, keyvalue);
            // 以适合使用 load 方法加载到 Properties 表中的格式，
            // 将此 Properties 表中的属性列表（键和元素对）写入输出流
            props.store(fos, "Update '" + keyname + "' value");
        } catch (IOException e) {
            log.error("属性文件更新错误" + e.getMessage());
        }
    }

    /**
     * 更新properties文件的键值对
     * 如果该主键已经存在，更新该主键的值；
     * 如果该主键不存在，则插件一对键值。
     *
     * @param keyname  键名
     * @param keyvalue 键值
     */
    public void updateProperties(String keyname, String keyvalue, String path) {
        try {
            props.load(new FileInputStream(path));
            // 调用 Hashtable 的方法 put，使用 getProperty 方法提供并行性。
            // 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
            OutputStream fos = new FileOutputStream(path);
            props.setProperty(keyname, keyvalue);
            // 以适合使用 load 方法加载到 Properties 表中的格式，
            // 将此 Properties 表中的属性列表（键和元素对）写入输出流
            props.store(fos, "Update '" + keyname + "' value");
        } catch (IOException e) {
            log.error("属性文件更新错误" + e.getMessage());
        }
    }
}
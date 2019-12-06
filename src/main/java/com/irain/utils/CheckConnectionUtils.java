package com.irain.utils;

import com.irain.conf.LoadConf;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/5 16:55
 * 检测设备状态配置文件
 **/
public class CheckConnectionUtils {


    public void saveLogtoFileByDay(String fileBasePath, String timeStr, String ip, String error) {

        try {
            String deviceName = new String(LoadConf.devicesMap.get(ip).getBytes("ISO-8859-1"), "UTF-8");
            String nowDay = TimeUtils.getStrNowtimeWithformat("yyyyMMdd");
            String fileName = fileBasePath + nowDay + ".txt";
            String writeLine = timeStr + "#" + ip + "#" + deviceName + "#" + error + '\n';
            FileUtils.writeFile(fileName, writeLine, true);
        } catch (UnsupportedEncodingException e) {
            System.out.println("保存设备日志时出现异常" + e.getMessage());
        }
    }
}
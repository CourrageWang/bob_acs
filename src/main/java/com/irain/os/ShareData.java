package com.irain.os;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/19 10:33
 * 局域网内实现数据备份
 * 共享步骤如下：
 * 将主机A的文件共享到主机B的目录
 * net use \\172.20.10.2 "123456" /user:"irain" 共享主机首次建立连接时需要与被共享主机之间建立连接
 * xcopy g:\\test.txt \\172.20.10.2\tmp
 * xcopy g:\\test.txt \\172.20.10.2\tmp /y && xcopy g:\\test2.txt \\172.20.10.2\tmp /y
 **/
public class ShareData {

    //获取文件存储路径
    public static String execCMD(String command) {

        StringBuilder sb = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (Exception e) {
            return e.toString();
        }
        return sb.toString();
    }
}
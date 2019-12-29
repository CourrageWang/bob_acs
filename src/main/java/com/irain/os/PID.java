package com.irain.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 根据进程名称杀死进程
 *
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/12 14:44
 **/
public class PID {

    //根据进程名称获取进程号
    public static boolean findAndKillProcess(String processName) {
        BufferedReader bufferedReader = null;
        try {
            String commd = "c:\\windows\\system32\\tasklist -fi " + '"' + "imagename eq " + processName + '"';
            System.out.println(commd);
            Process proc = Runtime.getRuntime().exec(commd);
            bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(processName)) {
                    killProcess(processName);
                    System.out.println("findProcess()获取到的进程信息：" + line);
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * 通过进程名称杀死进程
     *
     * @param processName
     */
    public static void killProcess(String processName) {
        try {
            if (processName != null) {
                Process pro = Runtime.getRuntime().exec("c:\\windows\\system32\\taskkill /F /im " + processName);
                BufferedReader brStd = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                BufferedReader brErr = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
                long time = System.currentTimeMillis();
                while (true) {
                    if (brStd.ready()) {
                        System.out.println("killProcess()进程正常返回:" + processName);
                        break;
                    }
                    if (brErr.ready()) {
                        System.out.println("killProcess()进程出错返回:" + processName);
                        break;
                    }
                    if (System.currentTimeMillis() - time > 3000) {
                        System.out.println("killProcess()等待超时:" + processName);
                        return;
                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PID().findAndKillProcess("explorer.exe");
    }
}

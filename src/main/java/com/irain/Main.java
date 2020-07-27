package com.irain;

import com.irain.conf.LoadConf;
import com.irain.task.TimeTask;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: w
 * @Date: 2019/11/14 3:48 下午
 * 启动类 同步数据与设备状态检测使用定时任务完成;
 */
@Log4j
public class Main {

    public static void main(String[] args) {

        /***
         *   思路:
         *    程序运行时 启动一个端口，并占用端口，每间隔一个小时 会重新启动程序，
         *    如果程序正常运行的话，那么 程序会抛出端口已占用的异常，如果程序结束的话
         *    则可正常启动。
         *    利用异常保证只有一个进程在运行。
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                Socket socket = null;
                try {
                    serverSocket = new ServerSocket(8888);
                    System.out.println("程序运行中。。。。。。");
                    socket = serverSocket.accept();//侦听并接受到此套接字的连接,返回一个Socket对象
                    System.out.println("程序运行中。。。。。。");
                } catch (IOException e) {
                    System.out.println("程序已经启动。。。。。");
                    System.exit(0);
                } finally {
                    try {
                        // 释放资源
                        serverSocket.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();


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
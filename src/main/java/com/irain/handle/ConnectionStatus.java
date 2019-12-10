package com.irain.handle;

import com.irain.net.impl.Instruction;
import com.irain.utils.*;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

/**
 * @Author: w
 * @Date: 2019/11/25 10:03 上午
 * 监测设备连接状态
 */
@Log4j
public class ConnectionStatus {

    private static final String END_FLAG = "E3";

    /**
     * 检测设备的连接状态
     *
     * @param ip
     * @param port
     * @return
     */
    public static Set<String> checkDeviceStatus(String ip, int port) {
        log.debug("检测设备状态。。。");
        Set<String> storeDataFromServer = new HashSet<>();
        //停止读取标志位
        boolean isEnd = true;
        //1.建立客户端socket连接，指定串口服务器地址及端口
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;

        try {
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            log.debug(String.format("连接设备%s:%s", ip, port));
            socket.connect(socketAddress, 3000);
            socket.setSoTimeout(5000);//5秒没有数据则将抛出异常
            //2.得到socket读写流
            os = socket.getOutputStream();
            is = socket.getInputStream();

            //3.发送查询指令到串口服务器

            //获取寄存器地址
            int location = Integer.valueOf(ip.split("\\.")[3]);
            String instruction = new Instruction().getDeviceTime(location);

            log.info("设备" + ip + ":" + port + "发送指令" + instruction);
            os.write(StringUtils.hexStringToByteArray(instruction));
            os.flush();
//            socket.shutdownOutput();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("发送指令等待结束出现异常" + e.getMessage());
            }

            /**  ------------读取串口返回数据-----------
             *  socket 通信中，数据包并非一次全部发送，因此在获取数据包的时候，
             *  需要根据服务端的输出标志位作为结束的判断条件。
             */
            StringBuilder tmpStr = new StringBuilder();
            while (isEnd) {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int readLength = is.read(buffer);
                outputStream.write(buffer, 0, readLength);

                byte[] bytes = outputStream.toByteArray();
                byte end = bytes[bytes.length - 1];
                //暂时存储数据
                for (byte b : bytes) {
                    String hex = StringUtils.byteToHex(b);
                    tmpStr.append(hex);
                }
                if (END_FLAG.equals(StringUtils.byteToHex(end).toUpperCase())) {
                    isEnd = false;
                    //存储数据做统一处理
                    storeDataFromServer.add(tmpStr.toString());
                    //获取寄存器时间，并做校验
                    log.info(String.format("设备%s:%s返回数据%s", ip, port, storeDataFromServer));
                    String deviceTimeStr = TimeUtils.getDeviceTimeByInput(storeDataFromServer.toString());
                    log.info(String.format("设备ip%s:%s系统时间为:%s", ip, port, deviceTimeStr));
                    //和当请系统时间进行比对，如果时差超过10秒则出发时间重置函数
                    String currentOSTime = TimeUtils.getStrNowtimeWithformat("yyyyMMddHHmmss");
                    log.info(String.format("%s:%s返回数据:%s转换后为:%s 程序系统运行时间为:%s", ip, String.valueOf(port)
                            , storeDataFromServer, deviceTimeStr, currentOSTime));
                    //判断时间是否合法
                    if (TimeUtils.isValidDate(deviceTimeStr, "yyyyMMddHHmmss")) {
                        if (TimeUtils.getSecondsBetweenTime(currentOSTime, deviceTimeStr) >= 10) {
                            log.info("程序相差超过10秒，将触发校时操作");
                            String correctTimeIns = new Instruction().correctTimeInstruction(1, currentOSTime);
                            log.debug("校时操作发送指令为" + correctTimeIns);

                            CommonUtils.closeStream(socket, br, is, os);
                            // 发送校时操作
//                            boolean b = CommonUtils.sendCommand(ip, port, correctTimeIns);
//                            if (b) {
//                                log.debug("校时操作成功");
//                            }
//                            log.debug("完成校时操作");
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            //包含异常 time out  与 connection refused 则将错误信息lost返回;
            if (e.getMessage().length() > 0) {

                // read读取阻塞 此时设备连接正常，只是由于网络原因未发送回报
                if (e.getMessage().contains("timed") && e.getMessage().contains("Read")) {
                    storeDataFromServer.add("line");
                    log.debug("阻塞读取超时");
                    return storeDataFromServer;
                }

                if (e.getMessage().contains("refused") || e.getMessage().contains("out")) {
                    storeDataFromServer.add("lost");
                    log.error("与设备网络通信时发生异常" + e.getMessage());
                    return storeDataFromServer;
                }
            }
        } finally {
            try {
                CommonUtils.closeStream(socket, br, is, os);
            } catch (IOException e) {
                log.error("关闭sock出现异常" + e.getMessage());
            }
        }
        return storeDataFromServer;
    }
}
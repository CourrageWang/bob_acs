package com.irain.handle;

import com.irain.net.impl.SerialSocketClient;
import com.irain.utils.StringUtils;
import com.irain.utils.TimeUtils;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: w
 * @Date: 2019/11/25 10:03 上午
 * 监测设备连接状态
 */
@Log4j
public class ConnectionStatus {

    private static final String DKJ_SUFFIX = "DKJ";

    private static final String END_FLAG = "E3";

    public static Set<String> checkDeviceStatus(String ip, int port) {
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
            socket.connect(socketAddress, 3000);
            //2.得到socket读写流
            os = socket.getOutputStream();
            is = socket.getInputStream();

            //3.发送查询指令到串口服务器
            String instruction = "D201005352";
            os.write(StringUtils.hexStringToByteArray(instruction));
            os.flush();

            /**  ------------读取串口返回数据-----------
             *  socket 通信中，数据包并非一次全部发送，因此在获取数据包的时候，
             *   需要根据服务端的输出标志位作为结束的判断条件。
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
                    log.info(String.format("get data form %s:%s data is %s", ip, port, storeDataFromServer));
                    String deviceTimeStr = TimeUtils.getDeviceTimeByInput(storeDataFromServer.toString());
                    log.info(String.format("device ip:%s port:%s system time is:%s", ip, port, deviceTimeStr));
                    //和当请系统时间进行比对，如果时差超过10秒则出发时间重置函数
                    String currentOSTime = TimeUtils.getStrNowtimeWithformat("yyyyMMddHHmmss");

                    log.info(String.format("device %s:%s source data is:%s covert time is:%s app runnig os time is:%s", ip, String.valueOf(port)
                            , storeDataFromServer, deviceTimeStr, currentOSTime));
                    if (TimeUtils.getSecondsBetweenTime(currentOSTime, deviceTimeStr) >= 10) {
                        log.info("time difference exceeds 10 seconds and trigger the time application");
                    }
                }
            }
            isEnd = true;//初始化读取标志位

        } catch (IOException e) {
            //包含异常 time out  与 connection refused 则将错误信息lost返回;
            if (e.getMessage().length() > 0) {
                if (e.getMessage().contains("refused") || e.getMessage().contains("out")) {
                    storeDataFromServer.add("lost");
                    return storeDataFromServer;
                }
            }
        } finally {
            try {
                //4.关闭资源
                if (socket != null) {
                    socket.close();
                }
                if (br != null) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return storeDataFromServer;
    }

    public static void check(Map<String, String> confMap) {
        confMap.forEach((k, v) -> {
            if (k.endsWith(DKJ_SUFFIX)) {
                //step1 读取门禁设备的Ip和port
                String[] addresses = StringUtils.getAddresses(v);
                String ip = addresses[0];
                String port = addresses[1];
                log.info(String.format(" try to connect device %s:%s ", ip, port));
                SerialSocketClient.getInfoFromDevice(ip, Integer.valueOf(port));
            }
        });
    }
}
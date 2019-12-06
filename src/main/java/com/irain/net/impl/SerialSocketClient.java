package com.irain.net.impl;

import com.irain.utils.CommonUtils;
import com.irain.utils.StringUtils;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: w
 * @Date: 2019/11/15 10:15 下午
 * 串口通信客户端
 */
@Log4j
public class SerialSocketClient {

    private static final int BUFFER_SIZE = 1024 * 2;
    private static final int BLOCK_SIZE = 3;
    private static final int REGION_SIZE = 255;
    private static final String END_FLAG = "E3";
    private static final String START_FLAG = "D2";
    private static final int JUMP_NUM = 240;//说明:在存储块0，区域240区域后数据无效

    /**
     * 指定的设备上，指定的块与区域上发送数据
     *
     * @param ip
     * @param port
     * @param block
     * @param region
     * @return
     */
    public static String getInfoFromDevice(String ip, int port, int block, int region) {

        log.info(String.format("尝试连接设备 %s:%s", ip, port));
        //停止读取标志位
        boolean isEnd = true;
        //1.建立客户端socket连接，指定串口服务器地址及端口
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            //连接socket 并指定连接时长为3秒
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            socket.connect(socketAddress, 3000);

            log.info(String.format("连接设备 %s:%s 成功", ip, port));
            //2.得到socket读写流
            os = socket.getOutputStream();
            is = socket.getInputStream();

            log.info("程序开始发送查询指令");

            int location = Integer.valueOf(ip.split("\\.")[3]);
            sendData(block, region, 1, os);
            StringBuilder tmpStr = new StringBuilder();

            while (isEnd) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int readLength = is.read(buffer);
                outputStream.write(buffer, 0, readLength);

                log.error(String.format("从 %s:%s 块: %d 区域: %d 获取的数据长度为： %d", ip, port, block, region, readLength));
                byte[] bytes = outputStream.toByteArray();
                byte end = bytes[bytes.length - 1]; //结束标志位

                for (byte b : bytes) {
                    String hex = StringUtils.byteToHex(b);
                    tmpStr.append(hex);
                }

                if ("E3".equals(StringUtils.byteToHex(end).toUpperCase())) {
                    return tmpStr.toString();
                }
            }
        } catch (IOException e) {
            log.error(String.format("连接设备%s:%s出现异常:%s", ip, port, e.getMessage()));
        } finally {
            try {
                CommonUtils.closeStream(socket, br, is, os);
            } catch (IOException e) {
                log.error("关闭sock出现异常" + e.getMessage());
            }
        }
        return "null";
    }


    /**
     * 发送查询指令
     *
     * @param block
     * @param region
     * @param os
     * @throws IOException
     */
    public static void sendData(int block, int region, int location, OutputStream os) throws IOException {
        String sendInfo = new Instruction().getSignInRecord(region, block, location);
        log.info(String.format("客户端发送指令为 %s 位于%d块区域 %d", sendInfo, block, region));
        os.write(StringUtils.hexStringToByteArray(sendInfo));
        os.flush();
        log.info("发送信息成功！");
    }
}
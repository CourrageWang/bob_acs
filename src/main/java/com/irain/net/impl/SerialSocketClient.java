package com.irain.net.impl;

import com.irain.utils.CommonUtils;
import com.irain.utils.StringUtils;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @Author: w
 * @Date: 2019/11/15 10:15 下午
 * 串口通信客户端
 */
@Log4j
public class SerialSocketClient {

    private static final String END_FLAG = "E3";

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
            socket.connect(socketAddress, 60000);
//            socket.setSoTimeout(5000);//读数据超时时间

            try {
                Thread.sleep(1000);
                System.out.println("程序休眠两秒");
            } catch (InterruptedException e) {
                log.error("休眠时发生异常！" + e.getMessage());
            }

            log.info(String.format("连接设备 %s:%s 成功", ip, port));
            //2.得到socket读写流
            os = socket.getOutputStream();
            is = socket.getInputStream();

            log.info("程序开始发送查询指令");

            int location = Integer.valueOf(ip.split("\\.")[3]);
            sendData(block, region, location, os);
            StringBuilder tmpStr = new StringBuilder();

            while (isEnd) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int readLength = is.read(buffer);
                outputStream.write(buffer, 0, readLength);

//                log.debug(String.format("从 %s:%s 块: %d 区域: %d 获取的数据长度为： %d", ip, port, block, region, readLength));
                byte[] bytes = outputStream.toByteArray();
                byte end = bytes[bytes.length - 1]; //结束标志位

                for (byte b : bytes) {
                    String hex = StringUtils.byteToHex(b);
                    tmpStr.append(hex);
                }

                if (END_FLAG.equals(StringUtils.byteToHex(end).toUpperCase())) {
                    return tmpStr.toString();
                }
            }
        } catch (IOException e) {
            log.error(String.format("连接设备%s:%s出现异常:%s", ip, port, e.getMessage()));
        } finally {
            try {
                //发送结束字符
//                log.debug("向服务端发送结束字符");
                os.write(StringUtils.hexStringToByteArray("00"));
                os.flush();
//                log.debug("开始关闭socket 连接");
                CommonUtils.closeStream(socket, br, is, os);
            } catch (IOException e) {
                log.error("关闭sock出现异常" + e.getMessage());
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
package com.irain.net.impl;

import com.irain.utils.StringUtils;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.Socket;

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
     * @param block
     * @param region
     * @return
     */
    public static String getInfoFromDevice(int block, int region, int location, OutputStream os, InputStream is) {

        //停止读取标志位
        boolean isEnd = true;
        //1.建立客户端socket连接，指定串口服务器地址及端口

        String sendRecord = "";
        try {

            log.info("程序开始发送查询指令");
            sendRecord = sendData(block, region, location, os);
            try {
                Thread.sleep(500);
                System.out.println("程序休眠一秒");
            } catch (InterruptedException e) {
                log.error("休眠时发生异常！" + e.getMessage());
            }

            StringBuilder tmpStr = new StringBuilder();

            while (isEnd) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int readLength = is.read(buffer);
                outputStream.write(buffer, 0, readLength);
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
            // 读取超时
            String errMsg = e.getMessage().split("\\s")[0];
            if ("Read".equals(errMsg)) {
                log.info("读取串口返回数据超时，程序将重发查询指令");
                try {
                    log.info("重发指令为" + sendRecord);
                    os.write(StringUtils.hexStringToByteArray(sendRecord));
                    os.flush();

                    while (isEnd) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int readLength = is.read(buffer);
                        outputStream.write(buffer, 0, readLength);

//                log.debug(String.format("从 %s:%s 块: %d 区域: %d 获取的数据长度为： %d", ip, port, block, region, readLength));
                        byte[] bytes = outputStream.toByteArray();
                        byte end = bytes[bytes.length - 1]; //结束标志位

                        StringBuilder tmpStr = new StringBuilder();
                        for (byte b : bytes) {
                            String hex = StringUtils.byteToHex(b);
                            tmpStr.append(hex);
                        }
                        if (END_FLAG.equals(StringUtils.byteToHex(end).toUpperCase())) {
                            return tmpStr.toString();
                        }
                    }
                } catch (IOException e1) {
                    log.error("数据发送异常！！！" + e1.getMessage());
                }
            }
        } finally {
            try {
                //发送结束字符
                os.write(StringUtils.hexStringToByteArray("00"));
                os.flush();

            } catch (IOException e) {
                log.error("关闭sock出现异常" + e.getMessage());
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //log.error(String.format("休眠出现异常", ip, port, e.getMessage()));
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
    public static String sendData(int block, int region, int location, OutputStream os) throws IOException {
        String sendInfo = new Instruction().getSignInRecord(region, block, location);
        log.info(String.format("客户端发送指令为 %s 位于%d块区域 %d", sendInfo, block, region));
        os.write(StringUtils.hexStringToByteArray(sendInfo));
        os.flush();
        log.info("发送信息成功！");
        return sendInfo;
    }
}
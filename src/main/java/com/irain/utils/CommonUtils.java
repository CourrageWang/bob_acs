package com.irain.utils;

import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/6 17:08
 * 抽离公共代码
 **/
@Log4j
public class CommonUtils {

    /**
     * 关闭资源
     *
     * @param socket
     * @param is
     * @param os
     * @throws IOException
     */
    public static void closeStream(Socket socket, InputStream is, OutputStream os) throws IOException {

        //4.关闭资源
        if (socket != null) {
            socket.close();
        }

        if (is != null) {
            is.close();
        }
        if (os != null) {
            os.close();
        }
    }

    /**
     * 给指定设备发送信息
     *
     * @param ip
     * @param port
     * @param command
     * @return
     */

    public static boolean sendCommand(String ip, int port, String command) {
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

            os.write(StringUtils.hexStringToByteArray(command));
            os.flush();
            StringBuilder tmpStr = new StringBuilder();

            while (isEnd) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int readLength = is.read(buffer);
                outputStream.write(buffer, 0, readLength);

                log.debug(String.format("从 %s:%s获取的数据长度为： %d", ip, port, readLength));
                byte[] bytes = outputStream.toByteArray();
                byte end = bytes[bytes.length - 1]; //结束标志位

                for (byte b : bytes) {
                    String hex = StringUtils.byteToHex(b);
                    tmpStr.append(hex);
                }
                if ("E3".equals(StringUtils.byteToHex(end).toUpperCase())) {
                    return true;
                }
            }
        } catch (IOException e) {
            log.error(String.format("连接设备%s:%s出现异常:%s", ip, port, e.getMessage()));
        } finally {
            try {
                CommonUtils.closeStream(socket, is, os);
            } catch (IOException e) {
                log.error("关闭sock出现异常" + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 解决中文乱码问题
     *
     * @param str
     * @return
     */
    public static String getCN(String str) {
        String deviceLocation = "";
        try {
            deviceLocation = new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("转码失败" + e.getMessage());
        }
        return deviceLocation;
    }
}
package com.irain.net.impl;

import com.irain.utils.StringUtils;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

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
    private static final int JUMP_NUM = 240;//说明:在存储块0，区域240区域后数据无效
    private static Set<String> storeDataFromServer = new HashSet<>();

    public static Set<String> getInfoFromDevice(String ip, int port) {

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

            log.info(String.format("connect device %s:%s success", ip, port));
            //2.得到socket读写流
            os = socket.getOutputStream();
            is = socket.getInputStream();

            log.info("start to send query sign instruction");
            for (int block = 0; block <= BLOCK_SIZE; block++) {
                for (int region = 0; region <= REGION_SIZE; region++) {

                    //跳过块0区域240数据块后的数据
                    if (block == 0 && region > JUMP_NUM) {
                        break;
                    }
                    //3.发送查询指令到串口服务器
                    String sendInfo = new SignInInstruction().getSignInRecord(region, block);
                    log.info(String.format("client send message is %s and block is %d region is %d",
                            sendInfo, block, region));
                    os.write(StringUtils.hexStringToByteArray(sendInfo));
                    os.flush();

                    /**  ------------读取串口返回数据-----------
                     *  socket 通信中，数据包并非一次全部发送，因此在获取数据包的时候，
                     *   需要根据服务端的输出标志位作为结束的判断条件。
                     */
                    StringBuilder tmpStr = new StringBuilder();
                    while (isEnd) {

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int readLength = is.read(buffer);
                        outputStream.write(buffer, 0, readLength);
                        log.info(String.format("get data from device %s:%s block: %d region: %d and length is %d", ip, port, block, region, readLength));

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
                        }
                    }
                    isEnd = true;//初始化读取标志位
                }
            }
        } catch (IOException e) {
            log.error(String.format("when connect %s%d has occured error", ip, port) + e.getMessage());
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
                log.error(String.format("when connect %s:%d has occured error", ip, port) + e.getMessage());
            }
        }
        return storeDataFromServer;
    }
}
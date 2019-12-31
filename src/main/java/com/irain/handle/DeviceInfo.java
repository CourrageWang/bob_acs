package com.irain.handle;

import com.irain.conf.LoadConf;
import com.irain.net.impl.SerialSocketClient;
import com.irain.os.ShareData;
import com.irain.utils.*;
import lombok.extern.log4j.Log4j;
import org.omg.CORBA.TIMEOUT;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/4 20:43
 **/
@Log4j
public class DeviceInfo {
    public static final int CORRECT_LENGTH = 256; //一个区块对应16*16条数据。
    public static final String YYYYMMDD = "yyyyMMdd";
    //区块大小以及无效数据区域临界值
    public static final int BLOCK_SIZE = 3;
    public static final int REGION_SIZE = 255;
    public static final int JUMP_INDEX = 240;

    //控制器端口号
    private static final int PORT = Integer.valueOf(LoadConf.propertiesMap.get("PORT").trim());

    public static final String BACKUPS_ADDRESS = LoadConf.propertiesMap.get("BACKUPS_ADDRESS_DK");

    public static final String DK_ADDRESS = LoadConf.propertiesMap.get("BACKUPS_KQ");

    /**
     * 获取所有设备的打卡数据
     *
     * @param confMap
     * @param yesterDatStr 获取指定日期的数据
     */
    public void loadAllDeviceData(Map<String, String> confMap, String yesterDatStr) {
        confMap.forEach((k, v) -> {
            //step1 读取门禁设备的Ip和port
            String ip = k;
            String infoFromDevice = "";
            int port = PORT;

            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            log.info(String.format("成功连接设备%s:%s", ip, port));
            OutputStream os = null;
            InputStream is = null;
            try {
                socket.connect(socketAddress, 6000);
                socket.setSoTimeout(10000);//读数据超时时间
                //2.得到socket读写流
                os = socket.getOutputStream();
                is = socket.getInputStream();
            } catch (IOException e) {
                //连接异常或者读超时异常。
                log.error(String.format("连接设备%s:%s出现异常", ip, port) + e.getMessage());
            }
            String recordPath = LoadConf.propertiesMap.get("READ_RECORD"); // 存储块区域的配置文件

            //从配置文件读取属性 不存在则从头开始读
            loop:
            for (int block = 0; block <= BLOCK_SIZE; block++) {
                for (int region = 0; region <= REGION_SIZE; region++) {
                    //跳过0区域240块的无效数据
                    if (block == 0 && region > JUMP_INDEX) {
                        break;
                    }
                    boolean isTrue = true;
                    while (isTrue) {
                        int location = Integer.valueOf(ip.split("\\.")[3]);
                        infoFromDevice = SerialSocketClient.getInfoFromDevice(block, region, location, os, is);
                        if (infoFromDevice != "null") { //数据不为空并且数据中不能含有字母
                            String start = infoFromDevice.substring(0, 2);
                            if ("e2".equals(start)) {
                                isTrue = false;
                                log.info(String.format("从设备 %s:%s块%s区%s", ip, port, block, region) + "数据合法为：" + infoFromDevice +
                                        "length " + infoFromDevice.length());

                                /**
                                 * 按照季度创建文件夹 并在其中存储对应的excel文件。
                                 */

                                String dkPath = DK_ADDRESS + TimeUtils.getYearWithSeason();
                                //log.debug("存储打卡数据的路径为" + dkPath);
                                String excelName = "";
                                try {
                                    excelName = new String(LoadConf.devicesMap.get(ip).getBytes("ISO-8859-1"), "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                String excelFilePath = dkPath + "\\" + ip.split("\\.")[3] + "_" + excelName + ".xls";//一楼门口_1.xls
                                // log.debug(String.format("Excel文件名称为%s路径为:%s", excelName, excelFilePath));
                                new DEVInfoUtils().createFolderWithExcels(dkPath, excelFilePath, ip);

                                //判断是否存在整页数据为空的现象
                                String x = infoFromDevice;
                                if (x.substring(2, x.length() - 2).startsWith("ff")) {
                                    break loop;
                                }
                                String strWithoutIdentifier = x.replaceAll("e2", "").replaceAll("e3", "");
                                int strLen = strWithoutIdentifier.length();
                                //判断数据长度是否符要求，不满足将在末尾追加数据
                                if (strLen != CORRECT_LENGTH) {
                                    strWithoutIdentifier = new DEVInfoUtils().appendZeroToEnd(strWithoutIdentifier);
                                }

                                for (int i = 0; i < strLen; i = i + 16) {
                                    String substring = strWithoutIdentifier.substring(i, i + 16);
                                    if (substring.startsWith("bb55") || substring.startsWith("b5b5")) {
                                        continue;
                                    }

                                    if (substring.startsWith("ff")) {
                                        break loop;
                                    }
                                    //获取时间
                                    String signTime = "20" + substring.substring(4, 6) + "-" + substring.substring(6, 8) + "-" +
                                            substring.substring(8, 10) + " " + substring.substring(10, 12) + ":" + substring.substring(12, 14);

                                    String signDay = "20" + substring.substring(4, 10);
                                    String cardNo = substring.substring(0, 4);//卡号
//                                    log.debug("卡号：" + cardNo + "打卡时间:" + signTime + "打卡日期：" + signDay);
                                    //时间合法则进行操作
                                    if (TimeUtils.isValidDate(signDay, YYYYMMDD)) {
                                        if (TimeUtils.compareDate(yesterDatStr, signDay, YYYYMMDD) == 0) {

                                            //创建excel文档按照季度设备名称创建
                                            try {
                                                new DEVInfoUtils().saVeDataToExcelBySeason(ip, cardNo, signTime, excelFilePath);
                                            } catch (UnsupportedEncodingException e) {
                                                log.error("写入数据到excel文件失败" + e.getMessage());
                                            }
                                        } else {
                                            // 数据不符合要求， 跳过本次循环
                                            continue;
                                        }
                                    }
                                }
                            }
                            // 如果设备连接异常返回null 继续检测下一个数据
                        } else if (infoFromDevice.equals("null")) {
                            log.error(String.format("设备%s:%s连接异常", ip, port + ""));
                            break loop;
                        }
                    }
                }
            }

            // 备份数据
            String dkPath = "E:\\data-dk\\" + TimeUtils.getYearWithSeason() + " ";
            String kqCommandTxt = "xcopy " + dkPath + BACKUPS_ADDRESS + "\\" + TimeUtils.getYearWithSeason() + "\\ " + " /y";
            log.info("开始备份数据-》指令为:" + kqCommandTxt);
            log.info(ShareData.execCMD(kqCommandTxt));

            //结束关闭连接
            try {
                log.info("关闭连接！！");
                CommonUtils.closeStream(socket, is, os);
            } catch (IOException e) {
                log.error(String.format("关闭连接%s:%s出现异常", ip, port));
            }
        });
    }
}
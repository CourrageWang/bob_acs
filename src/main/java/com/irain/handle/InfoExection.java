package com.irain.handle;

import com.irain.conf.LoadConf;
import com.irain.entity.FileInfo;
import com.irain.net.impl.SerialSocketClient;
import com.irain.os.ShareData;
import com.irain.utils.*;
import lombok.extern.log4j.Log4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

/**
 * @Author: w
 * @Date: 2019/11/14 5:30 下午
 * 核心处理类
 */
@Log4j
public class InfoExection {

    private static final String UNDER_LINE = "_";
    private static final String FILENAME_PREFIX = "DKJ_DKJL_";
    private static final String TEXT_FILE_SUFFIX = ".txt";
    private static final String VERF_FILE_SUFFIX = ".verf";
    private static final String DEVICE_ADDRESS = "XA";

    public static final int BLOCK_SIZE = 3;
    public static final int REGION_SIZE = 255;
    public static final int JUMP_INDEX = 240;

    public static final String YYYYMMDD = "yyyyMMdd";

    public static final char SPACE = (char) Integer.parseInt("1");
    public static final String VERF_SPACE = " ";
    public static final char END_LINE = (char) Integer.parseInt("10");

    //控制器端口号
    private static final int PORT = Integer.valueOf(LoadConf.propertiesMap.get("PORT").trim());
    //卡号到行号对应关系
    public static final String ACCOUNT_REL = LoadConf.propertiesMap.get("CARD_ACCOUNT");
    //获取文件存储路径[考勤数据存储路径]
    public static final String FILEPATH = LoadConf.propertiesMap.get("FILE_PATH");
    //备份主机的路径
    public static final String BACKUPS_ADDRESS = LoadConf.propertiesMap.get("BACKUPS_ADDRESS");
    //备份考勤数据的本机地址
    public static final String DATA_KQ_BACKUPS = LoadConf.propertiesMap.get("FILE_BACKUPS_PATH");
    // 非本行员工标志位
    public static final String ILLEGAL_USER = "000000";

    private static List<String> signsList = new ArrayList<>(); //全局静态资源使用需谨慎

    /**
     * 考勤数据因读的控制器较少，为了保证数据的可靠性，采用全读的方式。
     *
     * @param confMap
     * @param loadTime
     * @return
     */
    public static void execute(Map<String, String> confMap, String loadTime) {

        confMap.forEach((k, v) -> {
            //step1 读取门禁设备的Ip和port
            String ip = k;
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
            //创建socket
            String infoFromDevice = "";

            lable:
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

                        if ("null".equals(infoFromDevice)) { //如果返回"null"则可认为设备连接异常；
                            log.error(String.format("设备%s:%s连接异常", ip, port + ""));
//                            break lable;
                            region++;
                        }
                        String start = infoFromDevice.substring(0, 2);
                        if ("e2".equals(start)) {
                            isTrue = false;
                            log.info(String.format("从设备 %s:%s", ip, 10001) + "数据合法为：" + infoFromDevice);

                            //判断是否存在整页数据为空的现象
                            String x = infoFromDevice;
                            if (x.substring(2, x.length() - 2).startsWith("ff")) {
                                break lable;
                            }

                            String strWithoutIdentifier = x.replaceAll("e2", "").replaceAll("e3", "");
                            int strLen = strWithoutIdentifier.length();
                            //判断数据长度是否符要求，不满足将在末尾追加数据
                            if (strLen % 16 != 0) {
                                strWithoutIdentifier = new DEVInfoUtils().appendZeroToEnd(strWithoutIdentifier);
                            }
                            for (int i = 0; i < strLen; i = i + 16) {

                                String substring = strWithoutIdentifier.substring(i, i + 16);
                                if (substring.startsWith("bb55") || substring.startsWith("b5b5") || substring.startsWith("aa55") || substring.startsWith("a5a5")) {
                                    continue;
                                }
                                // 如果以ff开头则跳出循环
                                if (substring.startsWith("ff")) {
                                    continue lable;
                                }

                                //获取时间
                                String signTime = "20" + substring.substring(4, 6) + "-" + substring.substring(6, 8) + "-" +
                                        substring.substring(8, 10) + " " + substring.substring(10, 12) + ":" + substring.substring(12, 14) + ":00";

                                String signDay = "20" + substring.substring(4, 10);
                                String cardNo = substring.substring(0, 4);//卡号
//                                log.debug("卡号：" + cardNo + "打卡时间:" + signTime + "打卡日期：" + signDay);
                                //时间合法则进行操作
                                if (TimeUtils.isValidDate(signDay, YYYYMMDD)) {
                                    if (TimeUtils.compareDate(loadTime, signDay, YYYYMMDD) == 0) {
                                        System.err.println("当前时间为：" + loadTime + "siginDay:" + signDay);
                                        String userAccount = PropertyUtils.readValue(ACCOUNT_REL, cardNo);
                                        if (userAccount != null) {
                                            if (userAccount.length() > 0) {
                                                if (!ILLEGAL_USER.equals(userAccount.split("@")[0])) {
                                                    String sub = userAccount.split("@")[0] + "#" + signTime;
                                                    signsList.add(sub);
                                                }
                                            }
                                        }
                                    } else {
                                        // 数据不符合要求,跳过本次循环
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            try {
                log.info("关闭连接！！");
                CommonUtils.closeStream(socket, is, os);
            } catch (IOException e) {
                log.error(String.format("关闭连接%s:%s出现异常", ip, port));
            }
        });

        if (signsList.size() > 0) {

            //如果为多个打卡机，需要与之前存储在文件中的签到信息做对比并返回。
            /*** String fileNamePrefix = new StringBuilder().append(FILENAME_PREFIX).append(DEVICE_ADDRESS)
             .append(UNDER_LINE + loadTime).toString();
             String textPathName = new StringBuilder().append(FILEPATH).append(fileNamePrefix)
             .append(TEXT_FILE_SUFFIX).toString();
             *  暂时去除，不在对功获取的数据进行筛选
             // 过滤数据返回有效数据
             ArrayListMultimap<String, String> almp = InfoParser.ParserData(signsList, textPathName);
             log.debug("整合后的日期为：" + almp.toString());
             */
            //获取打卡信息
            getSingsInfo(signsList, loadTime);
        }
    }

    /**
     * 获取打卡信息
     */
    public static void getSingsInfo(List<String> info, String loadDate) {

        //组合文件路径
        String fileNamePrefix = new StringBuilder().append(FILENAME_PREFIX).
                append(DEVICE_ADDRESS).append(UNDER_LINE + loadDate).toString();

        String textPathName = new StringBuilder().append(FILEPATH).append(fileNamePrefix).append(TEXT_FILE_SUFFIX).toString();
        String verfPathName = new StringBuilder().append(FILEPATH).append(fileNamePrefix).append(VERF_FILE_SUFFIX).toString();


        String textBackUpsPathName = new StringBuilder().append(DATA_KQ_BACKUPS).append(fileNamePrefix).append(TEXT_FILE_SUFFIX).toString();
        String verfBackUpsPathName = new StringBuilder().append(DATA_KQ_BACKUPS).append(fileNamePrefix).append(VERF_FILE_SUFFIX).toString();

        //生成打卡文件
        info.forEach(x -> {
            if (!x.isEmpty()) {
                String[] split = x.split("#");
                String[] time = split[1].split("\\s");

                String writeLine = split[0] + SPACE + time[0] + SPACE + time[1] + END_LINE;
                FileUtils.writeFile(textPathName, writeLine, true);
                // 再次备份打卡数据
                FileUtils.writeFile(textBackUpsPathName, writeLine, true);
            }
        });

        //生成校验文件
        FileInfo fileInfo = FileUtils.getFileInfo(textPathName);
        log.debug("文件 长度" + fileInfo.getRecordCounts() + "---" + fileInfo.getFileLength());
        if (fileInfo.getFileLength() > 0 && fileInfo.getRecordCounts() > 0) { //文件不为空
            //写校验文件
            String writeVerfStr = fileNamePrefix + TEXT_FILE_SUFFIX + VERF_SPACE + fileInfo.getFileLength() + VERF_SPACE +
                    String.valueOf(fileInfo.getRecordCounts() - 1) + VERF_SPACE + loadDate + END_LINE;
            FileUtils.writeFile(verfPathName, writeVerfStr, false);
            // 再次备份校验文件数据
            FileUtils.writeFile(verfBackUpsPathName, writeVerfStr, true);
        }
       /* //备份数据
        log.info("程序开始备份考勤数据");
        // xcopy d:\\access\\target\\DKJ_DKJL_XA_20191202.txt  \\192.168.0.168\data-kq /y;
        String kqCommandTxt = "xcopy " + "E:\\data-kq" + " " + BACKUPS_ADDRESS + " /y";
        log.info("开始备份数据-》指令为:" + kqCommandTxt);
        log.info(ShareData.execCMD(kqCommandTxt));*/

        //释放资源【由于定时任务结束后不会自动释放List中数据，程序需要手动释放】list 本身为静态资源全局生成不会释放
        log.debug("释放资源" + "释放前长度为：" + info.size());
        info.removeAll(info);
        log.debug("释放资源" + "释放前后度为：" + info.size());
    }
}
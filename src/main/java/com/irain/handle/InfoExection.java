package com.irain.handle;

import com.google.common.collect.ArrayListMultimap;
import com.irain.conf.LoadConf;
import com.irain.entity.FileInfo;
import com.irain.net.impl.SerialSocketClient;
import com.irain.utils.*;
import lombok.extern.log4j.Log4j;

import java.sql.Timestamp;
import java.text.ParseException;
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

    //控制器端口号
    private static final int PORT = Integer.valueOf(LoadConf.propertiesMap.get("PORT").trim());
    //卡号到行号对应关系
    public static final String ACCOUNT_REL = LoadConf.propertiesMap.get("CARD_ACCOUNT");
    //获取文件存储路径
    public static final String FILEPATH = LoadConf.propertiesMap.get("FILE_PATH");

    private static Set<String> signsSet = new HashSet<>();

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
            String infoFromDevice = "";
            int port = PORT;
            lable:
            for (int block = 0; block <= BLOCK_SIZE; block++) {
                for (int region = 0; region <= REGION_SIZE; region++) {

                    //跳过0区域240块的无效数据
                    if (block == 0 && region > JUMP_INDEX) {
                        break;
                    }

                    boolean isTrue = true;
                    while (isTrue) {
                        infoFromDevice = SerialSocketClient.getInfoFromDevice(ip, port, block, region);

                        if ("null".equals(infoFromDevice)) { //如果返回"null"则可认为设备连接异常；
                            log.error(String.format("设备%s:%s连接异常", ip, port + ""));
                            break lable;
                        }
                        String start = infoFromDevice.substring(0, 2);
                        if ("e2".equals(start)) {
                            isTrue = false;
                            log.info(String.format("从设备 %s:%s", ip, 10001) + "数据合法为：" + infoFromDevice);
                            String x = infoFromDevice;
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
                                        substring.substring(8, 10) + " " + substring.substring(10, 12) + ":" + substring.substring(12, 14);

                                String signDay = "20" + substring.substring(4, 10);
                                String cardNo = substring.substring(0, 4);//卡号
//                                log.debug("卡号：" + cardNo + "打卡时间:" + signTime + "打卡日期：" + signDay);
                                //时间合法则进行操作
                                if (TimeUtils.isValidDate(signDay, YYYYMMDD)) {
                                    if (TimeUtils.compareDate(loadTime, signDay, YYYYMMDD) == 0) {
                                        String userAccount = PropertyUtils.readValue(ACCOUNT_REL, cardNo);
                                        if (userAccount != null) {
                                            if (userAccount.length() > 0) {
                                                String sub = userAccount.split("@")[0] + "#" + signTime;
                                                signsSet.add(sub);
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
        });

        if (signsSet.size() > 0) {
            //如果为多个打卡机，需要与之前存储在文件中的签到信息做对比并返回。
            String fileNamePrefix = new StringBuilder().append(FILENAME_PREFIX).append(DEVICE_ADDRESS)
                    .append(UNDER_LINE + loadTime).toString();
            String textPathName = new StringBuilder().append(FILEPATH).append(fileNamePrefix)
                    .append(TEXT_FILE_SUFFIX).toString();
            // 过滤数据返回有效数据
            ArrayListMultimap<String, String> almp = InfoParser.ParserData(signsSet, textPathName);

            log.debug("整合后的日期为：" + almp.toString());
            //获取打卡信息
            getSingsInfo(almp, loadTime);
        }
    }

    /**
     * 获取打卡信息
     */
    public static void getSingsInfo(ArrayListMultimap<String, String> info, String loadDate) {

        //组合文件路径
        String fileNamePrefix = new StringBuilder().append(FILENAME_PREFIX).
                append(DEVICE_ADDRESS).append(UNDER_LINE + loadDate).toString();

        String textPathName = new StringBuilder().append(FILEPATH).append(fileNamePrefix).append(TEXT_FILE_SUFFIX).toString();
        String verfPathName = new StringBuilder().append(FILEPATH).append(fileNamePrefix).append(VERF_FILE_SUFFIX).toString();

        info.asMap().forEach((k, v) -> {

            v.forEach(z -> {
                try {
                    Date punchDate = TimeUtils.timeStrToDate(loadDate, "yyyyMMdd");
                    Timestamp punchTimeStamp = new Timestamp(TimeUtils.timeStrToDate(z, "yyyy-MM-dd HH:mm").getTime());

                    String writeLine = k + '\t' + punchDate + '\t' + punchTimeStamp + '\n';
                    // 生成记录写入文件
                    FileUtils.writeFile(textPathName, writeLine, true);

                } catch (ParseException e) {
                    log.error("日期格式转换异常" + e.getMessage());
                }
            });

        });

        //生成校验文件
        FileInfo fileInfo = FileUtils.getFileInfo(textPathName);
        log.debug("文件 长度" + fileInfo.getRecordCounts() + "---" + fileInfo.getFileLength());
        if (fileInfo.getFileLength() > 0 && fileInfo.getRecordCounts() > 0) { //文件为空
            //写校验文件
            String writeVerfStr = fileNamePrefix + TEXT_FILE_SUFFIX + '\t' + fileInfo.getFileLength() + '\t' +
                    String.valueOf(fileInfo.getRecordCounts() - 1) + '\t' + loadDate + '\n';
            FileUtils.writeFile(verfPathName, writeVerfStr, false);
        }
    }
}
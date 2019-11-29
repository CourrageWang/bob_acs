package com.irain.handle;


import com.irain.conf.LoadConf;
import com.irain.entity.VerfInfo;
import com.irain.net.impl.SerialSocketClient;
import com.irain.utils.FileUtils;
import com.irain.utils.StringUtils;
import com.irain.utils.TimeUtils;
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
    private static final String DKJ_SUFFIX = "DKJ";
    private static final String DEVICE_ADDRESS = "HPL";

    private static Set<String> signsSet = new HashSet<>();

    public static void execute(Map<String, String> confMap) {
        confMap.forEach((k, v) -> {
            //step1 读取门禁设备的Ip和port
            String ip = k;
            String port = 5000 + "";
            log.info(String.format("try to connect device %s:%s ", ip, port));
            signsSet = SerialSocketClient.getInfoFromDevice(ip, Integer.valueOf(port));

        });
        if (signsSet.size() > 0) {
            // 过滤数据返回有效数据
            Set<String> set = InfoParser.ParserData(signsSet);
            //获取打卡信息
            getSingsInfo(set);
        }
        log.info("load data end.....");
    }

    /**
     * 获取打卡信息
     */
    public static void getSingsInfo(Set<String> set) {

        //丛配置文件中获取需要查询的日期
        String readDate = LoadConf.propertiesMap.get("READ_DATE");
        String filePath = LoadConf.propertiesMap.get("FILE_PATH");
        Calendar cal = Calendar.getInstance();
        String year = cal.get(Calendar.YEAR) + ""; //获取当前年份
        VerfInfo verfInfo = new VerfInfo();
        //组合文件路径
        String fileNamePrefix = new StringBuilder().append(FILENAME_PREFIX).
                append(DEVICE_ADDRESS).append(UNDER_LINE + readDate).toString();

        String textPathName = new StringBuilder().append(filePath).append(fileNamePrefix).append(TEXT_FILE_SUFFIX).toString();
        String verfPathName = new StringBuilder().append(filePath).append(fileNamePrefix).append(VERF_FILE_SUFFIX).toString();
        set.forEach(x -> {
            //获取指时间的数据
            String timeStr = year.substring(0, 2) + x.substring(4, 6) + x.substring(6, 10);
            //定时任务执行前的前一天数据
            String loadDate = TimeUtils.getYesterDayStr();
            if (timeStr.equals(loadDate)) {
                String userAccount = x.substring(0, 4);
                String punchTimeStr = timeStr + x.substring(10, 14);
                Date punchDate = null;
                Timestamp punchTimeStamp = null;
                try {
                    punchDate = TimeUtils.timeStrToDate(punchTimeStr, "yyyyMMddHHmm");
                    punchTimeStamp = new Timestamp(punchDate.getTime());
                } catch (ParseException e) {
                    log.error("convert date err " + e.getMessage());
                }

                // 从Access 数据库中获取数据

                String writeLine = userAccount + '\t' + punchDate + '\t' + punchTimeStamp + '\n';
                // 生成记录写入文件
                FileUtils.writeFile(textPathName, writeLine);

                //生成校验文件
                if (verfInfo.getRecordCounts() == 0) {
                    verfInfo.setRecordCounts(1);
                } else {
                    verfInfo.setRecordCounts(verfInfo.getRecordCounts() + 1);
                }
                if (verfInfo.getFileLength() == 0) {
                    verfInfo.setFileLength(writeLine.length());
                } else {
                    verfInfo.setFileLength(writeLine.length() + verfInfo.getFileLength());
                }

            }
        });
        //写校验文件
        String writeVerfStr = fileNamePrefix + TEXT_FILE_SUFFIX + '\t' + verfInfo.getFileLength() + '\t' +
                verfInfo.getRecordCounts() + '\t' + readDate + '\n';
        FileUtils.writeFile(verfPathName, writeVerfStr);
    }
}
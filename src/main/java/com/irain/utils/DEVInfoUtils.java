package com.irain.utils;

import com.irain.conf.LoadConf;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/5 14:59
 * 设备信息工具类
 **/
@Log4j
public class DEVInfoUtils {

    public static final int CORRECT_LENGTH = 256; //一个区块对应16*16条数据。

    String basePath = LoadConf.propertiesMap.get("BACKUPS_KQ");

    //卡号到行号对应关系
    public static final String ACCOUNT_REL = LoadConf.propertiesMap.get("CARD_ACCOUNT");

    /**
     * 如果数据长度不满足16*16则在末尾加0
     *
     * @param data
     * @return
     */
    public String appendZeroToEnd(String data) {
        StringBuilder sb = new StringBuilder();
        sb.append(data);
        int len = data.length();
        if (len != CORRECT_LENGTH) { //如果长度不符合要求则在数据末尾添加0
            int index = CORRECT_LENGTH - len;
            for (int i = 0; i < index; i++) {
                sb.append("0");
            }
        }
        return sb.toString();
    }

    /**
     * 存储数据到excel文件中按照季度存储
     *
     * @param ip
     * @param cardNo
     * @param signTime
     * @throws UnsupportedEncodingException
     */
    public void saVeDataToExcelBySeason(String ip, String cardNo, String signTime, String filePath) throws UnsupportedEncodingException {
        String userRecord = PropertyUtils.readValue(ACCOUNT_REL, cardNo);
        String userName = "";
        if (userRecord == null) {
            userName = "";
        } else {
            log.debug("cardNo:" + cardNo + "userRecord" + userRecord);
            userName = new String(userRecord.split("@")[1].getBytes("ISO-8859-1"), "UTF-8");
        }
        //人员编号-日期-时间-控制器编号-姓名
        String writeLine = cardNo + "#" + signTime.substring(0, 10) + "#" + signTime.substring(11, 16) +
                "#" + ip.split("\\.")[3] + "#" + userName;
        //开始写数据
        new ExcelUtils().appendContentToExcel(filePath, ip, writeLine);
    }

    /***
     *
     * @param folderPath
     * @param ip
     */
    public void createFolderWithExcels(String folderPath, String excelFilePath, String ip) {
        // log.debug("存储打卡数据的文件夹为" + folderPath);
        FileUtils.createFolder(folderPath);
        File file = new File(excelFilePath);

        if (!file.exists()) {
            new ExcelUtils().createExcelWithSheets(excelFilePath, ip);
        }
//        log.debug("创建完成");
    }
}
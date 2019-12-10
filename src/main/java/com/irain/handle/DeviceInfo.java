package com.irain.handle;

import com.irain.conf.LoadConf;
import com.irain.net.impl.SerialSocketClient;
import com.irain.utils.DEVInfoUtils;
import com.irain.utils.PropertyUtils;
import com.irain.utils.TimeUtils;
import lombok.extern.log4j.Log4j;

import java.io.UnsupportedEncodingException;
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

    //获取上次存储的区块后，后移25区块后结束
    public static final int MOVE_INDEX = 25;

    //控制器端口号
    private static final int PORT = Integer.valueOf(LoadConf.propertiesMap.get("PORT").trim());

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
            String recordPath = LoadConf.propertiesMap.get("READ_RECORD"); // 存储块区域的配置文件
            PropertyUtils readRecord = new PropertyUtils(recordPath);

            //从配置文件读取属性 不存在则从头开始读
            if (readRecord.getKeyValue(ip) == null) {
                loop:
                for (int block = 0; block <= BLOCK_SIZE; block++) {
                    for (int region = 0; region <= REGION_SIZE; region++) {
                        //跳过0区域240块的无效数据
                        if (block == 0 && region > JUMP_INDEX) {
                            break;
                        }
                        boolean isTrue = true;
                        while (isTrue) {
                            infoFromDevice = SerialSocketClient.getInfoFromDevice(ip, port, block, region);

                            if (infoFromDevice != "null") { //数据不为空并且数据中不能含有字母
                                String start = infoFromDevice.substring(0, 2);
                                if ("e2".equals(start)) {
                                    isTrue = false;
                                    log.info(String.format("从设备 %s:%s块%s区%s", ip, port, block, region) + "数据合法为：" + infoFromDevice +
                                            "length " + infoFromDevice.length());

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
                                        log.debug("卡号：" + cardNo + "打卡时间:" + signTime + "打卡日期：" + signDay);
                                        //时间合法则进行操作
                                        if (TimeUtils.isValidDate(signDay, YYYYMMDD)) {
                                            if (TimeUtils.compareDate(yesterDatStr, signDay, YYYYMMDD) == 0) {
                                                //更新记录块文件
                                                readRecord.updateProperties(k, block + "@" + region, recordPath);
                                                //创建excel文档按照季度设备名称创建
                                                try {
                                                    new DEVInfoUtils().saVeDataToExcelBySeason(ip, cardNo, signTime);
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
            } else { // 配置文件中有属性
                // 获取属性值开始读文进
                log.error("从区块文件中获取记录");
                String recordValue = readRecord.getKeyValue(ip);
                int block = Integer.valueOf(recordValue.split("@")[0]);
                int region = Integer.valueOf(recordValue.split("@")[1]);
                int step = region + MOVE_INDEX; //从当前位置往后后移25块
                int end = 0;//最终结束标志
                //开始同步
                table:
                for (int block2 = block; block2 <= BLOCK_SIZE; block2++) {
                    for (int region2 = region; region2 <= REGION_SIZE + 1; region2++) {
                        //跳过无效区域
                        if (block2 == 0 && region2 > JUMP_INDEX) {
                            break;
                        }
                        //读到最后一个区块后，重新寻找临界点 [设置为256 否则不发送3块255区域数据，设值成功并跳出本次循环]
                        if (block2 == 3 && region2 == REGION_SIZE + 1) {
                            block2 = 0;
                            region2 = 0;
                            continue;
                        }
                        end = step > REGION_SIZE ? (step - REGION_SIZE) : step;
                        if (end == region2) {
                            break table;
                        }
                        boolean isTrue = true;
                        while (isTrue) {
                            infoFromDevice = SerialSocketClient.getInfoFromDevice(ip, port, block2, region2);
                            if (infoFromDevice != "null") { //数据不为空并且数据中不能含有字母
                                String start = infoFromDevice.substring(0, 2);

                                if ("e2".equals(start)) {
                                    isTrue = false;
                                    log.info(String.format("从设备 %s:%s块%s区%s", ip, port, block2, region2) + "数据合法为：" + infoFromDevice +
                                            "length " + infoFromDevice.length());
                                    //判断数据是否为空
                                    String x = infoFromDevice;
                                    if (x.substring(2, x.length() - 2).startsWith("ff")) {
                                        break table;
                                    }

                                    String strWithoutIdentifier = x.replaceAll("e2", "").replaceAll("e3", "");
                                    int strLen = strWithoutIdentifier.length();

                                    //判断数据长度是否符要求，不满足将在末尾追加数据
                                    if (strLen != CORRECT_LENGTH) {
                                        strWithoutIdentifier = new DEVInfoUtils().appendZeroToEnd(strWithoutIdentifier);
                                    }

                                    for (int i = 0; i < strLen; i = i + 16) {
                                        String substring = strWithoutIdentifier.substring(i, i + 16);
                                        if (substring.startsWith("bb55") || substring.startsWith("b5b5") || substring.startsWith("ff")) {
                                            continue;
                                        }
                                        //获取时间
                                        String signTime = "20" + substring.substring(4, 6) + "-" + substring.substring(6, 8) + "-" +
                                                substring.substring(8, 10) + " " + substring.substring(10, 12) + ":" + substring.substring(12, 14);
                                        String signDay = "20" + substring.substring(4, 10);
                                        String cardNo = substring.substring(0, 4);//卡号

                                        //时间合法则进行操作
                                        if (TimeUtils.isValidDate(signDay, YYYYMMDD)) {
                                            if (TimeUtils.compareDate(yesterDatStr, signDay, YYYYMMDD) == 0) {
                                                //更新记录块文件
                                                readRecord.updateProperties(k, block + "@" + region, recordPath);
                                                //创建excel文档按照季度设备名称创建
                                                try {
                                                    new DEVInfoUtils().saVeDataToExcelBySeason(ip, cardNo, signTime);
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
                                break table;
                            }
                        }
                    }
                }
            }
        });
    }
}
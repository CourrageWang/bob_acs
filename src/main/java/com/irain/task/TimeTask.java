package com.irain.task;

import com.irain.conf.LoadConf;
import com.irain.handle.ConnectionStatus;
import com.irain.handle.DeviceInfo;
import com.irain.handle.InfoExection;
import com.irain.swing.ErrorDialog;
import com.irain.utils.CheckConnectionUtils;
import com.irain.utils.CommonUtils;
import com.irain.utils.Player;
import com.irain.utils.TimeUtils;
import lombok.extern.log4j.Log4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: w
 * @Date: 2019/11/25 9:26 上午
 * 执行定时任务 定时检测各个设备的连接状态，以及定时执行文件导入操作；
 */
@Log4j
public class TimeTask {

    private static ScheduledExecutorService excutor = Executors.newSingleThreadScheduledExecutor();

    private static final String VOICE_DEVICE_ERROR = LoadConf.propertiesMap.get("HAPPENED_ERROR_VOCIE");
    private static final String VOICE_LOST_CONN = LoadConf.propertiesMap.get("LOST_CONNECTION_VOICE");
    private static final String FOLDER = LoadConf.propertiesMap.get("FILE_PATH");
    private static final String PORT = LoadConf.propertiesMap.get("PORT");
    private static final Map<String, String> devicesMap = LoadConf.devicesMap;

    // 程序备份数据路径

    /**
     * 检测设备连接状况并对时间误差超过10秒的门禁设备进行校时
     */
    public void checkConnection() {
        excutor.scheduleAtFixedRate(() -> {
                    try {
                        log.info("******检测设备连接状态定时任务程序开始执行******");
                        LoadConf.devicesMap.forEach((k, v) -> {
                            String ip = k;
                            String port = PORT;
                            log.info(String.format(" 开始检测设备数据%s:%s", ip, port));
                            Set<String> set = ConnectionStatus.checkDeviceStatus(ip, Integer.valueOf(port));
                            //设备连接异常 并写入文件按日生成
                            if (set.contains("lost")) {
                                new Player().playMP3Music(VOICE_LOST_CONN);

                                //弹出对话框
                                if (LoadConf.jFrame == null) {
                                    ErrorDialog.getInstance().label.setText(String.format("%s:%s连接异常", TimeUtils.getNowTimeStr(), CommonUtils.getCN(devicesMap.get(ip))));
                                }
                                ErrorDialog.label.setText(String.format("%s:%s连接异常", TimeUtils.getNowTimeStr(), CommonUtils.getCN(devicesMap.get(ip))));
                                //按天生成文件
                                new CheckConnectionUtils().saveLogtoFileByDay(FOLDER, TimeUtils.getNowTimeStr(), ip, "设备连接异常");
                            }
                            //设备故障并写入文件按日生成
                            if (set.size() == 0) {
                                new Player().playMP3Music(VOICE_DEVICE_ERROR);
                                //弹出对话框
                                if (LoadConf.jFrame == null) {
                                    ErrorDialog.getInstance().label.setText(String.format("%s:%s设备硬件异常", TimeUtils.getNowTimeStr(), CommonUtils.getCN(devicesMap.get(ip))));
                                }
                                ErrorDialog.label.setText(String.format("%s:%s设备硬件异常", TimeUtils.getNowTimeStr(), CommonUtils.getCN(devicesMap.get(ip))));
                                new CheckConnectionUtils().saveLogtoFileByDay(FOLDER, TimeUtils.getNowTimeStr(), ip, "设备硬件异常");
                            }
                        });
                        log.info("******检测设备连接状态定时任务结束******");
                    } catch (Exception e) {
                        log.error("检查设备状态时出现异常" + e.getMessage());
                    }
                },
                0,  //初始化延迟
                1 * 60 * 60 * 1000, //两次开始的执行的最小时间间隔
                TimeUnit.MILLISECONDS //计时单位
        );
    }

    /**
     * 每天指定时间获取考勤数据
     *
     * @param time
     */
    public void dayOfLoadSignData(String time) {

        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = TimeUtils.getTimeMillis(time) - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        excutor.scheduleAtFixedRate(() -> {
                    try {
                        log.info("******获取考勤数据定时任务开始执行******");
                        //开始处理输入数据
                        InfoExection.execute(LoadConf.importDevicesMap, TimeUtils.getYesterDayStr().trim());
                        log.info("******获取考勤数据定时任务执行结束*******");
                    } catch (Exception e) {
                        log.error("导入数据发生异常" + e.getMessage());
                    }
                },
                initDelay,  //初始化延迟
                oneDay, //两次开始的执行的最小时间间隔
                TimeUnit.MILLISECONDS //计时单位
        );
    }

    /**
     * 每天指定时间获取所有设备前一天的打卡数据
     *
     * @param time
     */
    public void dayOfLoadAllDeviceData(String time) {
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = TimeUtils.getTimeMillis(time) - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        excutor.scheduleAtFixedRate(() -> {
                    try {
                        log.info("~~~~~~定时任务开始导入打卡数据~~~~~~");
                        new DeviceInfo().loadAllDeviceData(LoadConf.devicesMap, TimeUtils.getYesterDayStr().trim());
                        log.info("~~~~~~定时任务开始导入打卡数据~~~~~~");
                    } catch (Exception e) {
                        log.error("定时任务执行过程中发生异常" + e.getMessage());
                    }
                },
                initDelay,  //初始化延迟
                oneDay, //两次开始的执行的最小时间间隔
                TimeUnit.MILLISECONDS //计时单位
        );
    }
}
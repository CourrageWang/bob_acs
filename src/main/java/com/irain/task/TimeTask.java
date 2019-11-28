package com.irain.task;

import com.irain.conf.LoadConf;
import com.irain.handle.ConnectionStatus;
import com.irain.handle.InfoExection;
import com.irain.utils.Player;
import com.irain.utils.StringUtils;
import lombok.extern.log4j.Log4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private static DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    private static DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
    private static ScheduledExecutorService excutor = Executors.newSingleThreadScheduledExecutor();

    private static final String VOICE_STREAM = "/Users/yqwang/Workspace/java/home_work_v2/src/main/resources/output.mp3";
    private static final String DKJ_SUFFIX = "DKJ";

    /**
     * 检测设备连接状况。
     */
    public void checkConnection() {
        excutor.scheduleAtFixedRate(() -> {
                    try {
                        //加载配置文件
                        new LoadConf();
                        LoadConf.propertiesMap.forEach((k, v) -> {
                            if (k.endsWith(DKJ_SUFFIX)) {
                                String[] addresses = StringUtils.getAddresses(v);
                                String ip = addresses[0];
                                String port = addresses[1];
                                log.info(String.format(" start check device status ip:%s port:%s", ip, port));
                                Set<String> set = ConnectionStatus.checkDeviceStatus(ip, Integer.valueOf(port));
                                if (set.size() == 0) {
                                    log.error(String.format("device ip:%s port:%s lost connect", ip, port));
                                    new Player().getVoice(ip, port);
                                    new Player().playMP3Music(VOICE_STREAM);
                                }
                            }
                        });

                    } catch (Exception e) {
                        log.error("when check device status has happened error" + e.getMessage());
                    }
                },
                0,  //初始化延迟
                1 * 60 * 60 * 1000, //两次开始的执行的最小时间间隔
                TimeUnit.MILLISECONDS //计时单位
        );
    }

    /**
     * 每天指定时间同部署数据
     *
     * @param time
     */
    public void dayOfLoadData(String time) {
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = getTimeMillis(time) - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        excutor.scheduleAtFixedRate(() -> {
                    try {
                        log.info("start time task dayOfLoadData");
                        log.info("--------irain net_pack_parser application start-----------");
                        //加载配置文件
                        new LoadConf();
                        //开始处理输入数据
                        InfoExection.execute(LoadConf.propertiesMap);
                    } catch (Exception e) {
                        log.error("time task has error when load Data");
                    }
                },
                initDelay,  //初始化延迟
                oneDay, //两次开始的执行的最小时间间隔
                TimeUnit.MILLISECONDS //计时单位
        );
    }

    /**
     * 获取给定时间对应的毫秒数
     *
     * @param time "HH:mm:ss"
     * @return
     */
    private static long getTimeMillis(String time) {
        try {
            Date currentDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return currentDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
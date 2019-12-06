package com.irain.net;

/**
 * @Author: w
 * @Date: 2019/11/18 5:22 下午
 * 定义指令集接口
 */
public interface InstrcutionInterface {
    /**
     * 读取打卡卡记录
     *
     * @param blockIndex  存储块位置
     * @param regionIndex 存储块对应的区域的
     * @param location    控制器地址
     * @return 每条查询指令
     */
    default String getSignInRecord(int regionIndex, int blockIndex, int location) {
        return null;
    }

    /**
     * 读取设备时间
     *
     * @param location 控制器地址
     * @return
     */
    default String getDeviceTime(int location) {
        return null;
    }

    /**
     * 控制器地址
     *
     * @param location 控制器地址
     * @param time     时间 格式为 201912021314150000
     * @return
     */
    default String correctTimeInstruction(int location, String time) {
        return null;
    }
}
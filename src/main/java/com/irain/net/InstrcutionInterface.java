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
     * @return 每条查询指令
     */
    default String getSignInRecord(int regionIndex, int blockIndex) {
        return null;
    }
}
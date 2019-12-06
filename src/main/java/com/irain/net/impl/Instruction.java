package com.irain.net.impl;

import com.irain.net.InstrcutionInterface;
import com.irain.utils.StringUtils;

/**
 * @Author: w
 * @Date: 2019/11/18 5:24 下午
 * 提取记录实现
 */
public class Instruction implements InstrcutionInterface {
    /***
     * 最后一位按照十进制求取异或关系;
     * @param regionIndex 存储块对应的区域的
     * @param blockIndex  存储块位置
     * @param location 控制器地址
     * @return
     */
    @Override
    public String getSignInRecord(int regionIndex, int blockIndex, int location) {
        StringBuilder sb = new StringBuilder();
        sb.append("D2");
        sb.append(StringUtils.setPrefix(Integer.toHexString(location)));
        sb.append("02");
        sb.append("58");
        sb.append(StringUtils.setPrefix(Integer.toHexString(regionIndex)));
        sb.append(StringUtils.setPrefix(Integer.toHexString(blockIndex)));
        //第七位为：2-6的异或 16进制的58对应十进制为88
        int tmp = (location ^ 02 ^ 88 ^ Integer.valueOf(Integer.toHexString(regionIndex), 16) ^ Integer.valueOf(Integer.toHexString(blockIndex), 16));
        sb.append(StringUtils.setPrefix(Integer.toHexString(tmp)));
        return sb.toString();
    }

    /**
     * 获取打卡设备的数据
     *
     * @param location
     * @return
     */
    @Override
    public String getDeviceTime(int location) {
        StringBuilder sb = new StringBuilder();
        sb.append("D2");
        sb.append(StringUtils.setPrefix(Integer.toHexString(location)));
        sb.append("00");
        sb.append("53");
        //第五位2到四位的异或
        int tmp = (location ^ 0 ^ 83);
        sb.append(StringUtils.setPrefix(Integer.toHexString(tmp)));
        return sb.toString();
    }

    /**
     * 校准寄存器地址指令
     *
     * @param location
     * @param time     时间格式为： 20191202131415 0000
     * @return
     */
    @Override
    public String correctTime(int location, String time) {
        StringBuilder sb = new StringBuilder();
        sb.append("D2");
        sb.append(Integer.toHexString(location));
        sb.append("10");
        sb.append("54");
        //time 201912221213450000
        StringBuffer sb2 = new StringBuffer();
        for (int i = 0; i < time.length(); i++) {
            sb2.append("0");
            sb2.append(time.charAt(i));
        }
        sb.append(sb2.toString());
        int tmp = (location ^ 10 ^ 54);
        return sb.toString();
    }
}
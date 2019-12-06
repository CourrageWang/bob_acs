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
     * @param location 控制器地址
     * @param time     时间格式为： 2019120213141500  2013120614244000
     * @return
     */
    @Override
    public String correctTimeInstruction(int location, String time) {
        //判断传入时间长度格式是否符合要求
        int timeLen = time.length();
        String[] instr = new String[21];
        if (timeLen != 16) {
            time = time + "00";
        }
        instr[0] = "D2";
        instr[1] = StringUtils.setPrefix(Integer.toHexString(location)); //第二位为控制器地址
        instr[2] = "10";//16进制字符串
        instr[3] = "54";//16进制字符串
        StringBuilder sb = new StringBuilder();
        int j = 4;
        // 第五位开始赋值时间，并给对应的数字位置前加上0
        for (int i = 0; i < time.length(); i++) {
            StringBuffer sb2 = new StringBuffer();
            sb2.append("0");
            sb2.append(time.charAt(i));
            instr[j] = sb2.toString();
            j++;
        }
        for (int i = 0; i < instr.length - 1; i++) {
            sb.append(instr[i]);
        }
        // 求异或求2-10的异或
        instr[20] = instr[1];
        int temp = Integer.valueOf(instr[20]);
        for (int k = 2; k < 20; k++) {
            if (k == 2) {
                instr[k] = "16"; //第二位10为16进制， 转换为10进制求异或
            }
            if (k == 3) {
                instr[k] = "84"; //第三位54为16进制，转换为10进制求异或关系
            }
            temp = temp ^ Integer.valueOf(instr[k]);
        }
        instr[20] = StringUtils.setPrefix(Integer.toHexString(temp)); //将所求异或关系转换为16进制后存储在第21位。
        sb.append(instr[20]);
        return sb.toString();
    }
}
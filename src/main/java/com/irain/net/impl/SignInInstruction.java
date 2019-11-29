package com.irain.net.impl;

import com.irain.net.InstrcutionInterface;
import com.irain.utils.StringUtils;

import java.nio.ByteBuffer;


/**
 * @Author: w
 * @Date: 2019/11/18 5:24 下午
 * 提取记录实现
 */
public class SignInInstruction implements InstrcutionInterface {
    @Override
    public String getSignInRecord(int regionIndex, int blockIndex) {
        StringBuilder sb = new StringBuilder();
        ByteBuffer b = ByteBuffer.allocate(48000);
        sb.append("D2");
        sb.append("01");
        sb.append("02");
        sb.append("58");
        sb.append(StringUtils.setPrefix(Integer.toHexString(regionIndex)));
        sb.append(StringUtils.setPrefix(Integer.toHexString(blockIndex)));
        //第七位为：2-6的异或 16进制的58对应十进制为88
        int tmp = (1 ^ 2 ^ 88 ^ Integer.valueOf(Integer.toHexString(regionIndex), 16) ^ Integer.valueOf(Integer.toHexString(blockIndex), 16));
        sb.append(StringUtils.setPrefix(Integer.toHexString(tmp)));
        return sb.toString();
    }
}
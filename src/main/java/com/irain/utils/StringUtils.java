package com.irain.utils;

/**
 * @Author: w
 * @Date: 2019/11/15 9:59 上午
 */
public class StringUtils {

    public static String[] getAddresses(String stringInfo) {
        if (!stringInfo.trim().isEmpty()) {
            String[] address = new String[2];
            String[] addressAndIp = stringInfo.trim().split("\\s+")[0].split(":");

            address[0] = addressAndIp[0];//IP
            address[1] = addressAndIp[1];//Port
            return address;
        }
        return null;
    }

    public static String setPrefix(String str) {
        if ("" != str || str.length() > 0) {
            if (str.length() == 1) {
                return "0" + str;
            }
            return str;
        }
        return "";
    }

    /**
     * 二进制字符串数组转换为字节数组
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToByteArray(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * 字节数组转换为字符串
     *
     * @param byteArray
     * @return
     */
    public static String toHexString(byte[] byteArray) {
        String str = null;
        if (byteArray != null && byteArray.length > 0) {
            StringBuffer stringBuffer = new StringBuffer(byteArray.length);
            for (byte byteChar : byteArray) {
                stringBuffer.append(String.format("%02X", byteChar));
            }
            str = stringBuffer.toString();
        }
        return str;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

        /**
         * 字节转换为16进制字符串
         *
         * @param b
         * @return
         */
        public static String byteToHex(byte b) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            return hex;
        }
}
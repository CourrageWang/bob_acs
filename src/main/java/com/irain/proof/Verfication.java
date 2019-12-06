package com.irain.proof;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/12/3 17:28
 **/

/**
 * 对服务端返回的数据校验
 */
public class Verfication {

    /**
     * 校验服务端返回数据的合法性
     *
     * @param dataStr
     * @return
     */
    public void isDataOk(String dataStr) {
        if (dataStr.length() > 0) {
            String strWithoutIdentifier = dataStr.replaceAll("e2", "").replaceAll("e3", "");
            int strLen = strWithoutIdentifier.length();


        }
    }
}
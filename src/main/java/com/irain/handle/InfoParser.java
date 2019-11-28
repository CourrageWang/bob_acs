package com.irain.handle;

import lombok.extern.log4j.Log4j;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: w
 * @Date: 2019/11/21 2:07 下午
 * 信息解析器
 */
@Log4j
public class InfoParser {


    private static final String START_IDENTIFIER = "e2";
    private static final String END_IDENTIFIER = "e3";
    private static final String INVALID_DATA = "f";

    private static final String MANUAL_OPEN = "aa55";
    private static final String PROGRAM_OPEN = "a5a5";
    private static final String EMPTY_DATA = "ff";

    public static Set<String> ParserData(Set<String> set) {

        if (set.size() > 0) {
            Set<String> returnSet = new HashSet<>();
            log.info("start to parser data ......");
            set.stream().filter(x -> x.startsWith(START_IDENTIFIER) && x.endsWith(END_IDENTIFIER)).
                    //过滤掉空的数据exp：[e2ffff...fffe3]
                            filter(s -> s.substring(2, s.length() - 2).startsWith(INVALID_DATA) == false).forEach(x -> {
                String strWithoutIdentifier = x.replaceAll(START_IDENTIFIER, "").replaceAll(END_IDENTIFIER, "");
                int strLen = strWithoutIdentifier.length();
                for (int i = 0; i < strLen; i = i + 16) {
                    String substring = strWithoutIdentifier.substring(i, i + 16);
                    if (substring.startsWith(MANUAL_OPEN) || substring.startsWith(PROGRAM_OPEN) || substring.startsWith(EMPTY_DATA)) {
                        continue;
                    }
                    //返回有效数据
                    returnSet.add(substring);
                }
            });
            return returnSet;
        }
        return null;
    }
}
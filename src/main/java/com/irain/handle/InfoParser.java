package com.irain.handle;

import com.irain.conf.LoadConf;
import com.irain.utils.FileUtils;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: w
 * @Date: 2019/11/21 2:07 下午
 * 信息解析器
 */
@Log4j
public class InfoParser {

    //卡号到行号对应关系
    public static final String ACCOUNT_REL = LoadConf.propertiesMap.get("CARD_ACCOUNT");

    /**
     * 解析指定日期的数据
     * 如果存在多个打卡设备的话，需要与上次已经存储的打卡信息进行对比，保证获取的打卡数据必须为多条打卡记录中的最大最小值。
     *
     * @param set
     * @param filePath 保存签到信息的文件路径
     * @return
     */
   /* public static ArrayListMultimap<String, String> ParserData_Bak(List<String> set, String filePath) {
        ArrayListMultimap<String, String> multimap = ArrayListMultimap.create(100, 1000);
        //从指定文件获取打开记录，如果文件存在则必有打卡记录

        File file = new File(filePath);
        if (file.exists()) {//读取打卡记录
            try {
                FileUtils.readFile(filePath).forEach(x -> {
                    //保存文件数据到set，并重新筛选时间
                    String[] str = x.split("\t");
                    String signTime = str[2].substring(0, 16);
                    set.add(str[0] + "#" + signTime);
                });
                log.debug("保存文件到内存并准备删除原始文件");
                boolean delete = file.delete();
                log.debug("删除文件状态" + delete);
            } catch (IOException e) {
                log.error(String.format("读取配置文件%s异常", filePath) + e.getMessage());
            }
        }
        set.stream().forEach(item -> {
            String[] split = item.split("#");
            multimap.put(split[0], split[1]);
        });
        ArrayListMultimap<String, String> multimap2 = ArrayListMultimap.create(100, 1000);
        for (String key : multimap.keySet()) {
            List<String> strings = multimap.get(key);
            List<String> collect = strings.stream().sorted().collect(Collectors.toList());

            if (collect.size() > 1) {
                multimap2.put(key, collect.get(0));
                multimap2.put(key, collect.get(collect.size() - 1));
                continue;
            }
            multimap2.put(key, collect.get(0));
        }
        return multimap2;
    }*/

//    /**
//     * @param signList
//     * @return
//     */
//    public static List<String> ParserData(List<String> signList) {
//
//    }
}
package com.irain.utils;

import com.irain.entity.FileInfo;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: w
 * @Date: 2019/11/15 5:02 下午
 * 读取文件配置类
 */
@Log4j
public class FileUtils {
    /**
     * 读取文件
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static List<String> readFile(String path) throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IOException(e);
        }
        return lines;
    }

    /**
     * 追加文件
     *
     * @param path
     * @param content
     * @throws IOException
     */
    public static void writeFile(String path, String content, boolean isAppend) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件is
            writer = new FileWriter(path, isAppend);
            writer.write(content);
            log.info(String.format("成功写入文件 %s 写入长度为 %d ", path, content.length()));
        } catch (IOException e) {
            log.error(String.format("写文件%s发生异常", path) + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.error(String.format("写文件%s发生异常", path) + e.getMessage());
            }
        }
    }

    /**
     * 读取配置文件
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static Map<String, String> getProperties(String filePath) throws Exception {
        Map map = new HashMap<String, Integer>();
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(new File(filePath)));
            p.load(in);
            p.entrySet().forEach((entry) -> map.put(entry.getKey(), entry.getValue()));
            return map;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new Exception(e.getMessage());
                }
            }
        }
    }

    public static FileInfo getFileInfo(String filePath) {
        File file = new File(filePath);
        FileReader fileReader = null;
        LineNumberReader lineNumberReader = null;

        if (file.exists()) {
            try {
                fileReader = new FileReader(file);
                lineNumberReader = new LineNumberReader(fileReader);
                lineNumberReader.skip(Long.MAX_VALUE);
                int lines = lineNumberReader.getLineNumber() + 1;
                long length = file.length();
                return new FileInfo(length, lines);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (lineNumberReader != null) {
                    try {
                        lineNumberReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return new FileInfo(0, 0);
    }
}
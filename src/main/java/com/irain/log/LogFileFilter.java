package com.irain.log;

import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.FileFilter;

/**
 * @Author: w
 * @Date: 2019/11/14 3:58 下午
 */
public class LogFileFilter implements FileFilter {
    private String logName;

    public LogFileFilter(String logName) {
        this.logName = logName;
    }

    public boolean accept(File file) {
        if (this.logName != null && !file.isDirectory()) {
            LogLog.debug(file.getName());
            return file.getName().startsWith(this.logName);
        } else {
            return false;
        }
    }
}
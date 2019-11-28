package com.irain.log;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Author: w
 * @Date: 2019/11/14 3:55 下午
 */
public class DailyRollingFileAppender extends FileAppender {

    private final String LOGFILE_SUFFIX =".log";
    private final int LOG_STORAGE_TIME = 5;

    private String datePattern = "yyyy-MM-dd";
    private int maxBackupIndex = 1;
    private String scheduledFilename;
    private long nextCheck = System.currentTimeMillis() - 1L;
    Date now = new Date();
    SimpleDateFormat sdf;
    RollingCalendar rc = new RollingCalendar();
    int checkPeriod = -1;
    static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

    public DailyRollingFileAppender() {
    }

    public DailyRollingFileAppender(Layout layout, String filename, String datePattern) throws IOException {
        super(layout, filename, true);
        this.datePattern = datePattern;
        this.activateOptions();
    }

    public void setDatePattern(String pattern) {
        this.datePattern = pattern;
    }

    public String getDatePattern() {
        return this.datePattern;
    }

    public int getMaxBackupIndex() {
        return this.maxBackupIndex;
    }

    public void setMaxBackupIndex(int maxBackupIndex) {
        this.maxBackupIndex = maxBackupIndex;
    }

    public void activateOptions() {
        super.activateOptions();
        if (this.datePattern != null && this.fileName != null) {
            this.now.setTime(System.currentTimeMillis());
            this.sdf = new SimpleDateFormat(this.datePattern);
            int type = this.computeCheckPeriod();
            this.printPeriodicity(type);
            this.rc.setType(type);
            File file = new File(this.fileName);
            if (this.fileName.endsWith(LOGFILE_SUFFIX)) {
                this.scheduledFilename = this.fileName.replace(LOGFILE_SUFFIX,
                        "_"+this.sdf.format(new Date(file.lastModified()))+LOGFILE_SUFFIX);
            }
        } else {
            LogLog.error("Either File or DatePattern options are not set for appender [" + this.name + "].");
        }

    }

    void printPeriodicity(int type) {
        switch(type) {
            case 0:
                LogLog.debug("Appender [" + this.name + "] to be rolled every minute.");
                break;
            case 1:
                LogLog.debug("Appender [" + this.name + "] to be rolled on top of every hour.");
                break;
            case 2:
                LogLog.debug("Appender [" + this.name + "] to be rolled at midday and midnight.");
                break;
            case 3:
                LogLog.debug("Appender [" + this.name + "] to be rolled at midnight.");
                break;
            case 4:
                LogLog.debug("Appender [" + this.name + "] to be rolled at start of week.");
                break;
            case 5:
                LogLog.debug("Appender [" + this.name + "] to be rolled at start of every month.");
                break;
            default:
                LogLog.warn("Unknown periodicity for appender [" + this.name + "].");
        }

    }

    int computeCheckPeriod() {
        RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.getDefault());
        Date epoch = new Date(0L);
        if (this.datePattern != null) {
            for(int i = 0; i <= LOG_STORAGE_TIME; ++i) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.datePattern);
                simpleDateFormat.setTimeZone(gmtTimeZone);
                String r0 = simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
                String r1 = simpleDateFormat.format(next);
                if (r0 != null && r1 != null && !r0.equals(r1)) {
                    return i;
                }
            }
        }

        return -1;
    }

    void rollOver() throws IOException {
        if (this.datePattern == null) {
            this.errorHandler.error("Missing DatePattern option in rollOver().");
        } else {

            String datedFilename = this.fileName.replace(LOGFILE_SUFFIX,
                    "_"+this.sdf.format(this.now)+LOGFILE_SUFFIX);

            if (!this.scheduledFilename.equals(datedFilename)) {
                this.closeFile();
                File target = new File(this.scheduledFilename);
                if (target.exists()) {
                    target.delete();
                }

                File file = new File(this.fileName);
                boolean result = file.renameTo(target);
                if (result) {
                    LogLog.debug(this.fileName + " -> " + this.scheduledFilename);
                } else {
                    LogLog.error("Failed to rename [" + this.fileName + "] to [" + this.scheduledFilename + "].");
                }

                if (file.getParentFile().exists()) {
                    File[] files = file.getParentFile().listFiles(new LogFileFilter(file.getName()));
                    Long[] dateArray = new Long[files.length];

                    int i;
                    for(i = 0; i < files.length; ++i) {
                        File fileItem = files[i];
                        String fileDateStr = fileItem.getName().replace(file.getName(), "");
                        Date filedate = null;

                        try {
                            filedate = this.sdf.parse(fileDateStr);
                            long fileDateLong = filedate.getTime();
                            dateArray[i] = fileDateLong;
                        } catch (ParseException var14) {
                            LogLog.error("Parse File Date Throw Exception : " + var14.getMessage());
                        }
                    }

                    Arrays.sort(dateArray);
                    if (dateArray.length > this.maxBackupIndex) {
                        for(i = 0; i < dateArray.length - this.maxBackupIndex; ++i) {
                            String dateFileName = file.getPath() + this.sdf.format(dateArray[i]);
                            File dateFile = new File(dateFileName);
                            if (dateFile.exists()) {
                                dateFile.delete();
                            }
                        }
                    }
                }

                try {
                    this.setFile(this.fileName, true, this.bufferedIO, this.bufferSize);
                } catch (IOException var13) {
                    this.errorHandler.error("setFile(" + this.fileName + ", true) call failed.");
                }

                this.scheduledFilename = datedFilename;
            }
        }
    }

    protected void subAppend(LoggingEvent event) {
        long n = System.currentTimeMillis();
        if (n >= this.nextCheck) {
            this.now.setTime(n);
            this.nextCheck = this.rc.getNextCheckMillis(this.now);

            try {
                this.rollOver();
            } catch (IOException var5) {
                if (var5 instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }

                LogLog.error("rollOver() failed.", var5);
            }
        }

        super.subAppend(event);
    }
}

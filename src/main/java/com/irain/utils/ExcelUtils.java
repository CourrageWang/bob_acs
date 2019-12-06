package com.irain.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @version: V1.0
 * @author: 王勇琪
 * @date: 2019/11/28 17:27
 **/
public class ExcelUtils {
    /**
     * 创建指定位置的Excel文件并包含多个Sheets
     *
     * @param filePath
     * @param listsOfSheets
     */
    public void createExcelWithSheets(String filePath, String sheetName) {

        if (filePath.length() > 0 && filePath.endsWith(".xls")) {
            FileOutputStream out = null;
            try {
                Workbook workbook = new XSSFWorkbook();//创建excel文件对象
                //创建sheets
                workbook.createSheet(sheetName);
                //创建每一张表格的第一行

                Sheet sheet = workbook.getSheet(sheetName);
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("人员编号");
                row.createCell(1).setCellValue("日期");
                row.createCell(2).setCellValue("控制器号");

                out = new FileOutputStream(filePath);
                workbook.write(out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 向EXcel 表格中添加数据
     *
     * @param content   追加的内容
     * @param filePath  文件路径
     * @param sheetName
     */
    public void appendContentToExcel(String filePath, String sheetName, String content) {
        // excel文件追加数据
        FileOutputStream out = null;
        XSSFWorkbook wb = null;
        try {
            FileInputStream fs = new FileInputStream(filePath);
            wb = new XSSFWorkbook(fs);

            //获取到工作表，因为一个excel可能有多个工作表
            XSSFSheet sheet = wb.getSheet(sheetName);
            //获取第一行（excel中的行默认从0开始，所以这就是为什么，一个excel必须有字段列头），即，字段列头，便于赋值
            XSSFRow row;
            out = new FileOutputStream(filePath);  //向文件中写数据
            row = sheet.createRow((short) (sheet.getLastRowNum() + 1)); //在现有行号后追加数据

            String[] split = content.split("#");
            for (int i = 0; i < split.length; i++) {
                row.createCell(i).setCellValue(split[i]);
            }
            out.flush();
            wb.write(out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (wb != null) {
                try {
                    wb.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
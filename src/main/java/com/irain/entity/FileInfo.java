package com.irain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: w
 * @Date: 2019/11/15 9:46 上午
 * 文件信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    private long fileLength;//文件的大小
    private int recordCounts;//文件包含记录数
}
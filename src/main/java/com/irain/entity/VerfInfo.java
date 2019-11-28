package com.irain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: w
 * @Date: 2019/11/15 9:46 上午
 * 校验文件信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerfInfo {
    private String dataFileName;//数据文件名称
    private int fileLength;//文件的大小
    private int recordCounts;//文件包含记录数
    private String DataTime;//数据日期
}
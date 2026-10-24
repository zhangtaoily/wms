package com.wms.service.common.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;
import java.util.Date;

import java.io.Serializable;

/**
 * 菜单编码视图对象 sys_menu_coding_attachments
 *
 * @author ruoyi
 * @date 2024-08-16
 */
@Data
@ExcelIgnoreUnannotated
public class SysMenuCodingAttachmentsVo implements Serializable {

    private static final long serialVersionUID = 1L;




    /**
     * 功能名称
     */
    @ExcelProperty(value = "功能名称")
    private String functionName;

    /**
     * 前缀
     */
    @ExcelProperty(value = "前缀")
    private String codingPrefix;

    /**
     * 编码前缀缀长度
     */
    @ExcelProperty(value = "编码前缀缀长度")
    private Long codingLength;

    /**
     * 前缀种类
     */
    @ExcelProperty(value = "前缀种类")
    private String codingType;

    /**
     * 顺序
     */
    @ExcelProperty(value = "顺序")
    private Long sort;


}

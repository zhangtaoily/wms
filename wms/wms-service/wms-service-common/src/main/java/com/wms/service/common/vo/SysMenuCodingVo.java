package com.wms.service.common.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.wms.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 菜单编码视图对象 sys_menu_coding
 *
 * @author ruoyi
 * @date 2024-08-16
 */
@Data
@ExcelIgnoreUnannotated
public class SysMenuCodingVo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */


    /**
     * 功能名称
     */
    @ExcelProperty(value = "功能名称")
    private String functionName;

    /**
     * 预览
     */
    @ExcelProperty(value = "预览")
    private String preview;

    /**
     * 编码次数
     */
    @ExcelProperty(value = "编码次数")
    private Integer codingNum;


}

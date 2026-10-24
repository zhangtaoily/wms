package com.wms.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.wms.common.annotation.ExcelDictFormat;
import com.wms.common.convert.ExcelDictConvert;
import com.wms.domain.SysFieldConfig;
import lombok.Data;

import java.io.Serializable;

/**
 * 字段配置表视图对象 sys_field_config
 *
 * @author ruoyi
 * @date 2024-07-31
 */
@Data
@ExcelIgnoreUnannotated
public class SysFieldConfigVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 从属功能组
     */
    @ExcelProperty(value = "从属功能组")
    private String moduleGroup;

    /**
     * 模块名称
     */
    @ExcelProperty(value = "模块名称")
    private String name;

    /**
     * 字段
     */
    @ExcelProperty(value = "字段")
    private String columnName;

    /**
     * 名称
     */
    @ExcelProperty(value = "名称")
    private String headerText;

    /**
     * 排序
     */
    @ExcelProperty(value = "排序")
    private Integer sortIndex;
    /**
     * 宽度
     */
    @ExcelProperty(value = "宽度")
    private Long columnWidth;
    /**
     * SQL表达式
     */
    @ExcelProperty(value = "SQL表达式")
    private String sqlColumn;

    @ExcelProperty(value = "字段类型")
    private String fieldType;

    @ExcelProperty(value = "默认值")
    private String defaultValue;

    public static SysFieldConfigVo valueOf(SysFieldConfig config) {
        if (config == null) {
            return null;
        }
        SysFieldConfigVo sysFieldConfigVo = new SysFieldConfigVo();
        sysFieldConfigVo.setId(config.getId());
        sysFieldConfigVo.setModuleGroup(config.getModuleGroup());
        sysFieldConfigVo.setName(config.getName());
        sysFieldConfigVo.setColumnName(config.getColumnName());
        sysFieldConfigVo.setHeaderText(config.getHeaderText());
        sysFieldConfigVo.setFieldType(config.getFieldType());
        sysFieldConfigVo.setDefaultValue(config.getDefaultValue());
        return sysFieldConfigVo;
    }
}

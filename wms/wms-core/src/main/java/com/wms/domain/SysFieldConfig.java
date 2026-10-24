package com.wms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.BaseEntity;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

/**
 * (SysFieldConfig)实体类
 *
 * @author makejava
 * @since 2024-07-31 14:15:21
 */
@Data
@TableName("sys_field_config")
public class SysFieldConfig extends BaseEntity {
    private static final long serialVersionUID = 384361153349734865L;

    /**
     * 从属功能组
     */
    @TableField("module_group")
    private String moduleGroup;
    /**
     * 模块名称
     */
    @TableField("name")
    private String name;

    @TableField("column_name")
    private String columnName;

    @TableField("header_text")
    private String headerText;

    @TableField("sort_index")
    private Integer sortIndex;

    @TableField("column_width")
    private String columnWidth;
    /**
     * SQL表达式
     */
    @TableField("sql_column")
    private String sqlColumn;

    @TableField("search")
    private Integer search;

    @TableField("dynamic")
    private Integer dynamic;

    @TableField("sort")
    private String sort;

    @TableField("field_type")
    private String fieldType;

    @TableField("format")
    private String format;

    @TableField("sql_query")
    private String sqlQuery;

    @TableField("default_value")
    private String defaultValue;

    @TableField("checked")
    private Integer checked;

    @TableField("order_by_index")
    private Integer orderByIndex;

    private Integer align;

    private Integer fixedColumn;

    private Integer columnSort;

    private Integer popover;
    /**
     * 是否显示
     */
    private Integer isShow;

    /**
     * PDA引擎：移动端是否显示（1=显示，PDA 列表/表单按此渲染，M6）
     */
    @TableField("mobile_show")
    private Integer mobileShow;

    /**
     * PDA引擎：扫码输入字段（1=PDA端渲染为扫码输入框唤起扫码，M6）
     */
    @TableField("scan_input")
    private Integer scanInput;

    /**
     * PDA引擎：移动端列序（PDA 按 mobile_seq 升序渲染，M6）
     */
    @TableField("mobile_seq")
    private Integer mobileSeq;

}


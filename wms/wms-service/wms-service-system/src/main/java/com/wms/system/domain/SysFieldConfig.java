/*
package com.wms.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.wms.common.core.domain.BaseEntity;

*/
/**
 * 字段配置表对象 sys_field_config
 *
 * @author ruoyi
 * @date 2024-07-31
 *//*

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_field_config")
public class SysFieldConfig extends BaseEntity {

    private static final long serialVersionUID=1L;

    */
/**
     * id
     *//*

    @TableId(value = "id")
    private String id;
    */
/**
     * 从属功能组
     *//*

    private String moduleGroup;
    */
/**
     * 模块名称
     *//*

    private String name;
    */
/**
     * 字段
     *//*

    private String columnName;
    */
/**
     * 名称
     *//*

    private String headerText;
    */
/**
     * 排序
     *//*

    private Long sortIndex;
    */
/**
     * 宽度
     *//*

    private Long columnWidth;
    */
/**
     * SQL表达式
     *//*

    private String sqlColumn;

}
*/

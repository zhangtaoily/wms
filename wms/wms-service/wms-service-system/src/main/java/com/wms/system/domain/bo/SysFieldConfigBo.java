package com.wms.system.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wms.common.core.validate.AddGroup;
import com.wms.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import com.wms.common.core.domain.BaseEntity;

/**
 * 字段配置表业务对象 sys_field_config
 *
 * @author ruoyi
 * @date 2024-07-31
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysFieldConfigBo extends BaseEntity {

    /**
     * id
     */
    @NotBlank(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 从属功能组
     */
    @NotBlank(message = "从属功能组不能为空", groups = { AddGroup.class, EditGroup.class })
    private String moduleGroup;

    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 字段
     */
    @NotBlank(message = "字段不能为空", groups = { AddGroup.class, EditGroup.class })
    private String columnName;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String headerText;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer sortIndex;

    /**
     * 宽度
     */
    @NotNull(message = "宽度不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer columnWidth;

    /**
     * SQL表达式
     */
    @NotBlank(message = "SQL表达式不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sqlColumn;

    private String format;

    private String defaultValue;

    private String fieldType;

    private boolean search;

    private boolean dynamic;

    private String sqlQuery;
}

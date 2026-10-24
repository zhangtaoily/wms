package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * (过滤方案模块详情)实体类
 *
 * @author makejava
 * @since 2024-12-31 12:07:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_filter_scheme_detail")
public class SysFilterSchemeDetail extends IdEntity {
    private static final long serialVersionUID = 577686048576634587L;

    /**
     * 过滤方案id
     */
    private Long filterSchemeId;
    /**
     * 过滤条件模块id
     */
    private Long fieldConfigId;
    /**
     * 运算符
     */
    private String operate;
    /**
     * 值
     */
    private String value;
    /**
     * 逻辑运算符
     */
    private String logicalOperator;
    /**
     * 排序
     */
    private Integer sortIndex;
}

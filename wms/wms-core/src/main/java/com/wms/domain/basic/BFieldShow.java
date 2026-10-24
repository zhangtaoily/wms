package com.wms.domain.basic;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 字段显示模块实体类
 *
 * @author makejava
 * @since 2024-11-20 14:13:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("b_field_show")
public class BFieldShow extends IdEntity implements Serializable {
    private static final long serialVersionUID = 615208107916821714L;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 字段配置表的id
     */
    private Long fieldConfigId;
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 是否显示
     */
    private Integer isShow;
    /**
     * 宽度
     */
    private Integer columnWidth;
    /**
     * 排序
     */
    private Integer sortIndex;
}

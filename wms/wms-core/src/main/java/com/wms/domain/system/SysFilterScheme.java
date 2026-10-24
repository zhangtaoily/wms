package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * (过滤方案模块)实体类
 *
 * @author makejava
 * @since 2024-12-31 12:07:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_filter_scheme")
public class SysFilterScheme extends BaseEntity {
    private static final long serialVersionUID = -17371582584783076L;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 模块名称
     */
    private String functionName;
    /**
     * 方案名称
     */
    private String schemeName;
    /**
     * 是否默认查询条件
     */
    private Integer isDefault;
    /**
     * 是否显示列表
     */
    private Integer isShow;
    /**
     * 是否共享
     */
    private Integer isShare;
}

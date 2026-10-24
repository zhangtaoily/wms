package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.BaseEntity;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【请填写功能名称】对象 sys_print_config
 *
 * @author ruoyi
 * @date 2024-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_print_config")
public class SysPrintConfig extends BaseEntity {

    private static final long serialVersionUID=1L;
    /**
     * 模块表名
     */
    private String modelName;
    /**
     * 链接地址
     */
    private String linkUrl;
    /**
     * 按钮名称
     */
    private String buttonName;
    /**
     * 是否启用  0-禁用  1-启用
     */
    private Integer enabled;

}

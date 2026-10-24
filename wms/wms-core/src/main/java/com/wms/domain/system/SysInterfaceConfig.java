package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.*;
import com.wms.common.core.domain.BaseEntity;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 【请填写功能名称】对象 sys_InterfaceConfig
 *
 * @author ruoyi
 * @date 2024-08-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_interface_config")
public class SysInterfaceConfig extends BaseEntity {

    private static final long serialVersionUID=1L;
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 用途
     */
    private String purposeId;
    /**
     * 提供方组织
     */
    private Long organizationId;
    /**
     * 提供方系统
     */
    private String systemId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 地址
     */
    private String url;

}

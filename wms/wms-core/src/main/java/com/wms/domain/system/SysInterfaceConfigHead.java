package com.wms.domain.system;
import com.baomidou.mybatisplus.annotation.*;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.wms.common.core.domain.IdEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_interface_config_head")
public class SysInterfaceConfigHead extends IdEntity {
    private static final long serialVersionUID=1L;

    /**
     *
     */
    private String itemName;
    /**
     *
     */
    private String itemValue;
    /**
     *
     */
    private Long interfaceId;
}

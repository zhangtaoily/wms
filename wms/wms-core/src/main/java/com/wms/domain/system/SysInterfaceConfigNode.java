package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_interface_config_node")
public class SysInterfaceConfigNode extends IdEntity {
    private static final long serialVersionUID=1L;

    /**
     *
     */
    private Long interfaceId;
    /**
     *
     */
    private String nodeName;
    /**
     *
     */
    private String parentNode;
    /**
     *
     */
    private String dataNode;
    /**
     *
     */
    private Integer sortIndex;
    private String dataType;
}

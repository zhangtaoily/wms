package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_interface_config_node_parm")
public class SysInterfaceConfigNodeParm extends IdEntity {
    private static final long serialVersionUID=1L;

    /**
     *
     */
    private Long nodeId;
    /**
     *
     */
    private Integer sortIndex;
    /**
     *
     */
    private String parmGroup;
    /**
     *
     */
    private String parmKey;
    /**
     *
     */
    private Integer dataType;
    /**
     *
     */
    private String dataFun;
    /**
     *
     */
    private String remark;
}

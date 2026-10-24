package com.wms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;

/**
 * (SysInterfaceLog)实体类
 *
 * @author makejava
 * @since 2024-08-15 13:21:27
 */
@Data
@TableName("sys_interface_log")
public class SysInterfaceLog extends IdEntity {
    private static final long serialVersionUID = 960387686359222328L;

    private Long interfaceId;

    private Integer version;
    @TableField("`index`")
    private Integer index;

    private String status;

    private String orgId;

    private String systemId;

    private String purposeId;

    private String url;

    private LocalDateTime requestTime;

    private String requestHead;

    private String requestParm;

    private LocalDateTime responseTime;

    private String responseMsg;

    private String responsePactCode;

    private Long pactId;

    private String modelId;

    private Boolean isSort;

    private LocalDateTime createTime;

    private String pactCode;
}


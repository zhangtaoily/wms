package com.wms.domain.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

/**
 * (自动编码)实体类
 *
 * @author makejava
 * @since 2024-08-28 09:53:32
 */
@Data
@TableName("sys_auto_code_set")
public class SysAutoCodeSet extends IdEntity {
    private static final long serialVersionUID = -58744990777936436L;

    private String functionName;

    private Integer isAutoCode;

    private String prefix;

    private Integer serialLen;
}

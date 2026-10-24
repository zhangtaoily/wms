package com.wms.common.core.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class IdEntity implements Entity{
    @TableId()
    private Long id;

    public static String getEntityTableName() {
        return null;
    }

}

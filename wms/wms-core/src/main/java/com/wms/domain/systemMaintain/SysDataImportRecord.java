package com.wms.domain.systemMaintain;

import com.wms.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 数据导入记录表实体类
 * @author li shuang
 */
@Data
public class SysDataImportRecord extends BaseEntity {
    /**
     * 合同编码
     */
    private String code;
    /**
     * 如果存在，是否更新数据
     */
    private Integer isUpdate;
    /**
     * 结果
     */
    private String result;
}

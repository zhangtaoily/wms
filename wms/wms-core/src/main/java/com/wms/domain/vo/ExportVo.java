package com.wms.domain.vo;

import com.wms.common.core.domain.PageQuery;
import com.wms.common.utils.ListUtils;
import lombok.Data;

import java.util.List;

/**
 * @author li shuang
 */
@Data
public class ExportVo {
    /**
     * ids
     */
    List<Long> ids;
    /**
     * 状态
     */
    String isAll;

    List<PageQuery.ConditionItem> conditions = ListUtils.empty();
}

package com.wms.base.view.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.AsrsWhTask;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 立库仓储任务数据视图：asrs_wh_task 单表
 */
public class AsrsWhTaskDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<AsrsWhTask> whTaskTable = dataViewMetaData.table(AsrsWhTask.class);
        dataViewMetaData.columns(getColumns()).from(whTaskTable);
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.ASRS_WH_TASK;
    }
}

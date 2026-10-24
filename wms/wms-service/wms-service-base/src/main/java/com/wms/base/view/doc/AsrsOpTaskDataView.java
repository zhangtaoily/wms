package com.wms.base.view.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.AsrsOpTask;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 立库作业任务数据视图：asrs_op_task 单表
 */
public class AsrsOpTaskDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<AsrsOpTask> opTaskTable = dataViewMetaData.table(AsrsOpTask.class);
        dataViewMetaData.columns(getColumns()).from(opTaskTable);
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.ASRS_OP_TASK;
    }
}

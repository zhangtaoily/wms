package com.wms.base.view.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.AsrsOpLog;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 立库作业日志数据视图：asrs_op_log 单表
 */
public class AsrsOpLogDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<AsrsOpLog> opLogTable = dataViewMetaData.table(AsrsOpLog.class);
        dataViewMetaData.columns(getColumns()).from(opLogTable);
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.ASRS_OP_LOG;
    }
}

package com.wms.base.view;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.InvBatch;
import com.wms.base.domain.InvBatchStock;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 库存批次数据视图：inv_batch 左联 inv_batch_stock（批次×仓位明细）
 */
public class InvBatchDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<InvBatch> batchTable = dataViewMetaData.table(InvBatch.class);
        Table<InvBatchStock> stockTable = dataViewMetaData.table(InvBatchStock.class);
        dataViewMetaData.columns(getColumns()).from(batchTable)
            .leftJoin(stockTable, TableInfoHelper.getTableInfo(InvBatchStock.class).getTableName() + ".inv_batch_id = "
                + TableInfoHelper.getTableInfo(InvBatch.class).getTableName() + ".id");
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.INV_BATCH;
    }
}

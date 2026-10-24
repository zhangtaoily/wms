package com.wms.base.view.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocStockMove;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 移库单数据视图：doc_stock_move 单表
 */
public class DocStockMoveDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<DocStockMove> moveTable = dataViewMetaData.table(DocStockMove.class);
        dataViewMetaData.columns(getColumns()).from(moveTable);
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.WMS_STOCK_MOVE;
    }
}

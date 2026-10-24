package com.wms.base.view.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocPalletOrder;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 组托单数据视图：doc_pallet_order 单表
 */
public class DocPalletOrderDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<DocPalletOrder> orderTable = dataViewMetaData.table(DocPalletOrder.class);
        dataViewMetaData.columns(getColumns()).from(orderTable);
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.WMS_PALLET_ORDER;
    }
}

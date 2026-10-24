package com.wms.base.view.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocOutNotice;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 出库通知单数据视图：doc_out_notice 单表
 */
public class DocOutNoticeDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<DocOutNotice> noticeTable = dataViewMetaData.table(DocOutNotice.class);
        dataViewMetaData.columns(getColumns()).from(noticeTable);
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.WMS_OUT_NOTICE;
    }
}

package com.wms.base.view.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.SyncRecord;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 同步监控数据视图：sync_record 单表（M7）
 */
public class SyncRecordDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<SyncRecord> recordTable = dataViewMetaData.table(SyncRecord.class);
        dataViewMetaData.columns(getColumns()).from(recordTable);
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.SYNC_RECORD;
    }
}

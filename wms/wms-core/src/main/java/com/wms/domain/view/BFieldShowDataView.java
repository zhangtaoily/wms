package com.wms.domain.view;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.basic.BFieldShow;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

public class BFieldShowDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();

        Table<SysFieldConfig> fieldConfigTable = dataViewMetaData.table(SysFieldConfig.class);
        Table<BFieldShow> fieldShowTable = dataViewMetaData.table(BFieldShow.class);
        dataViewMetaData.columns(getColumns()).from(fieldShowTable)
            .leftJoin(fieldConfigTable, TableInfoHelper.getTableInfo(SysFieldConfig.class).getTableName() + ".id = " + TableInfoHelper.getTableInfo(BFieldShow.class).getTableName() + ".field_config_id");

        return dataViewMetaData;
    }


    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.B_FIELD_SHOW;
    }
}

package com.wms.base.view;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.BaseLocation;
import com.wms.base.domain.BaseWarehouse;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.view.AbstractDataView;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

/**
 * 仓位信息数据视图：base_location 左联 base_warehouse
 */
public class LocationDataView extends AbstractDataView {

    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<BaseLocation> locationTable = dataViewMetaData.table(BaseLocation.class);
        Table<BaseWarehouse> warehouseTable = dataViewMetaData.table(BaseWarehouse.class);
        dataViewMetaData.columns(getColumns()).from(locationTable)
            .leftJoin(warehouseTable, TableInfoHelper.getTableInfo(BaseWarehouse.class).getTableName() + ".id = "
                + TableInfoHelper.getTableInfo(BaseLocation.class).getTableName() + ".warehouse_id");
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.LOCATION;
    }
}

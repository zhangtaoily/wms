package com.wms.domain.view;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.system.SysEmployee;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

public class SysEmployeeDataView extends AbstractDataView {
    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();

        Table<SysEmployee> sysEmployeeTable = dataViewMetaData.table(SysEmployee.class);


        Table<SysEmployee> managerTable = dataViewMetaData.table(SysEmployee.class, "managerTable");

        dataViewMetaData.columns(getColumns()).from(sysEmployeeTable)
            .leftJoin(managerTable, managerTable.getAlias() + ".oa_id = " + TableInfoHelper.getTableInfo(SysEmployee.class).getTableName() + ".manager_id");
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.SYS_EMPLOYEE;
    }
}

package com.wms.domain.view;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.entity.SysUser;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.system.SysFilterScheme;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

public class SysFilterSchemeDataView extends AbstractDataView{
    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();

        Table<SysFilterScheme> filterSchemeTable = dataViewMetaData.table(SysFilterScheme.class);
        Table<SysUser> userCreateTable = dataViewMetaData.table(SysUser.class, "createUser");
        dataViewMetaData.columns(getColumns()).from(filterSchemeTable)
        .leftJoin(userCreateTable, "createUser.id = " + TableInfoHelper.getTableInfo(SysFilterScheme.class).getTableName() + ".user_id ");
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.SYS_FILTER_SCHEME;
    }
}

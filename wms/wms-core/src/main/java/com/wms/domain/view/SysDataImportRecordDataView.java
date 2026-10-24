package com.wms.domain.view;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.entity.SysDictData;
import com.wms.common.core.domain.entity.SysUser;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysBusinessflowConfig;
import com.wms.domain.systemMaintain.SysDataImportRecord;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

import static com.wms.common.utils.TableInfoUtils.getTableName;

/**
 * @author li shuang
 */
public class SysDataImportRecordDataView extends AbstractDataView {
    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();
        Table<SysUser> userCreateTable = dataViewMetaData.table(SysUser.class, "createUser");
        Table<SysUser> userUpdateTable = dataViewMetaData.table(SysUser.class, "updateUser");
        Table<SysDataImportRecord> mainTable = dataViewMetaData.table(SysDataImportRecord.class);
        String mainTableName = getTableName(SysDataImportRecord.class);

        dataViewMetaData.columns(getColumns()).from(mainTable)
            .leftJoin(userCreateTable, "createUser.id = " + mainTableName + ".create_by ")
            .leftJoin(userUpdateTable, "updateUser.id = " + mainTableName + ".update_by ");
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.SYS_DATA_IMPORT_RECORD;
    }
}

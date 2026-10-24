package com.wms.domain.view;

import com.wms.common.core.domain.entity.SysDictData;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysInterfaceLog;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

public class InterfaceLogDataView extends AbstractDataView{
    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();

        Table<SysInterfaceLog> salIntContRecord = dataViewMetaData.table(SysInterfaceLog.class);
        Table<SysDictData> tPurpose= dataViewMetaData.table(SysDictData.class,"purpose");//用途
        Table<SysDictData> tSystem= dataViewMetaData.table(SysDictData.class,"interfaceSys");//接收方系统
        Table<SysDictData> tOrganization= dataViewMetaData.table(SysDictData.class,"interfaceOrg");//接收方组织
        Table<SysDictData> tState= dataViewMetaData.table(SysDictData.class,"interfaceState");//状态
        dataViewMetaData.columns(getColumns()).from(salIntContRecord)
            .leftJoin(tPurpose,  " purpose.dict_value = sys_interface_log.purpose_id and  purpose.dict_type='sys_purpose' ")
            .leftJoin(tSystem,  " interfaceSys.dict_value = sys_interface_log.system_id and  interfaceSys.dict_type='interface_sys' ")
            .leftJoin(tOrganization,  " interfaceOrg.dict_value = sys_interface_log.org_id and  interfaceOrg.dict_type='interface_org' ")
            .leftJoin(tState,"interfaceState.dict_value=sys_interface_log.status and interfaceState.dict_type='interface_status'");
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.SYS_INTERFACE_LOG;
    }
}

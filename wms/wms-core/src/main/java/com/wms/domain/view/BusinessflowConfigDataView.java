package com.wms.domain.view;

import com.wms.common.core.domain.entity.SysDictData;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysBusinessflowConfig;
import com.wms.sql.DataViewMetaData;
import com.wms.sql.Table;

public class BusinessflowConfigDataView  extends AbstractDataView {
    public DataViewMetaData fetchDataView() {
        DataViewMetaData dataViewMetaData = new DataViewMetaData();

        Table<SysBusinessflowConfig> salIntContRecord = dataViewMetaData.table(SysBusinessflowConfig.class);
        Table<SysDictData> tModel= dataViewMetaData.table(SysDictData.class,"model");//模块
        Table<SysDictData> tCondition= dataViewMetaData.table(SysDictData.class,"conditionData");//触发条件
        Table<SysDictData> tSend= dataViewMetaData.table(SysDictData.class,"send");//分发模式
        dataViewMetaData.columns(getColumns()).from(salIntContRecord)
            .leftJoin(tModel,  " model.dict_value = sys_businessflow_config.model_id and  model.dict_type='model_type' ")
            .leftJoin(tCondition,  " conditionData.dict_value = sys_businessflow_config.work_timing and  conditionData.dict_type='work_timing' ")
            .leftJoin(tSend,  " send.dict_value = sys_businessflow_config.send_type and  send.dict_type='send_type' ");
        return dataViewMetaData;
    }

    @Override
    public FunctionEnum getSupportsType() {
        return FunctionEnum.SYS_BUSINESSFLOW_CFG;
    }
}

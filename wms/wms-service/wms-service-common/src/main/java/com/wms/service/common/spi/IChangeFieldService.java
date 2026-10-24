package com.wms.service.common.spi;

import com.wms.common.enums.FunctionEnum;
import com.wms.common.enums.dicts.ChangeTypeEnum;

import java.util.List;
import java.util.Map;

public interface IChangeFieldService {

    public Map getChangeFieldList(FunctionEnum functionEnum, List<ChangeTypeEnum> changeTypeEnumList);
    Map getFilteredChangeFieldList(FunctionEnum functionEnum, Map<String, Object> beforeChangeData, Map<String, Object> afterChangeData, String subTableName);
}

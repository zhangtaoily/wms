package com.wms.domain.view;

import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ApplicationContextHolder;
import com.wms.spi.ISysFieldConfigService;

import java.util.List;

public abstract class AbstractDataView {
    public abstract FunctionEnum getSupportsType();
    public List<String> getColumns() {
        return getColumnsByFunctionEnum(getSupportsType());
    }
    public List<String> getColumnsByFunctionEnum(FunctionEnum functionEnum) {
        ISysFieldConfigService sysFieldConfigService = ApplicationContextHolder.getBean(ISysFieldConfigService.class);
        return sysFieldConfigService.getWrapperColumns(functionEnum);
    }
}

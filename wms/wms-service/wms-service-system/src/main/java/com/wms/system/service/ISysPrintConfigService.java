package com.wms.system.service;

import com.wms.domain.system.SysPrintConfig;

import java.util.List;

public interface ISysPrintConfigService {
    public List<SysPrintConfig> selectPrintConfigByModelName(String modelName);
}

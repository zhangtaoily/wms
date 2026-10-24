package com.wms.service.common.spi;

import com.wms.common.enums.FunctionEnum;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IWfConfigService {
    void createWf(FunctionEnum functionName, Long id, Supplier<Long> supplier);
    void createWf(FunctionEnum functionName, Long id, Supplier<Long> supplier,Consumer<Long> consumer);
    void createWf(FunctionEnum functionName, Long id, Supplier<Long> supplier, Consumer<Long> consumer, Map<String,Object> params);

}

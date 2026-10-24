package com.wms.service.common.spi;

import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ApplicationContextHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用列表管理器
 */
public final class CommonFormManager {

    /**
     * 单例-实例
     */
    private static final CommonFormManager INSTANCE = new CommonFormManager();

    private final Map<FunctionEnum, ICommonFormService> formMap = new ConcurrentHashMap<>();

    private CommonFormManager() {
    }

    public static CommonFormManager getInstance() {
        return INSTANCE;
    }

    public void registerFormList(ICommonFormService formService) {
        formMap.put(formService.getSupportType(), formService);
    }

    public ICommonFormService getFormService(FunctionEnum functionEnum) {
        return ApplicationContextHolder.getBean(formMap.get(functionEnum).getClass());
    }
}

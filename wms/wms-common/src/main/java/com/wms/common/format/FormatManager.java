package com.wms.common.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * format管理器
 */
public final class FormatManager {

    /**
     * 单例-实例
     */
    private static final FormatManager INSTANCE = new FormatManager();

    private final Map<FormatterEnum, IFormatter> formatMap = new ConcurrentHashMap<>();

    private FormatManager() {
    }

    public static FormatManager getInstance() {
        return INSTANCE;
    }

    public void registerFormat(IFormatter format) {
        formatMap.put(format.getSupportType(), format);
    }

    public IFormatter getFormat(FormatterEnum formatEnum) {
        return formatMap.get(formatEnum);
    }
}

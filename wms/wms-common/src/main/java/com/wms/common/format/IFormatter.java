package com.wms.common.format;

import org.springframework.beans.factory.InitializingBean;

public interface IFormatter<T> extends InitializingBean {

    FormatterEnum getSupportType();

    String format(T rawValue);

    @Override
    default void afterPropertiesSet() throws Exception {
        FormatManager.getInstance().registerFormat(this);
    }

}

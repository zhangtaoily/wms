package com.wms.common.format;

public abstract class FormatSupport<T> implements IFormatter<T> {

    @Override
    public String format(T rawValue) {
        if (rawValue == null)
            return null;

        return doFormat(rawValue);
    }

    protected abstract String doFormat(T rawValue);
}

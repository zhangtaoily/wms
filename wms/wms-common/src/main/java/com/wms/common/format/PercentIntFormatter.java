package com.wms.common.format;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;

@Component
public class PercentIntFormatter extends FormatSupport<Object>{
    @Override
    protected String doFormat(Object rawValue) {
        // 获取当前区域的默认百分比格式
        NumberFormat percentFormat = NumberFormat.getPercentInstance();

        // 格式化百分比
        return percentFormat.format(rawValue);
    }

    @Override
    public FormatterEnum getSupportType() {
        return FormatterEnum.PERCENT;
    }
}

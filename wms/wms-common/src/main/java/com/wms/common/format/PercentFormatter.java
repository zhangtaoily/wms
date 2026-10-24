package com.wms.common.format;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;

@Component
public class PercentFormatter extends FormatSupport<Object>{
    @Override
    protected String doFormat(Object rawValue) {
        // 获取当前区域的默认百分比格式
        NumberFormat percentFormat = NumberFormat.getPercentInstance();

        // 设置百分比精度为2位小数
        percentFormat.setMinimumFractionDigits(2);

        // 格式化百分比
        return percentFormat.format(rawValue);
    }

    @Override
    public FormatterEnum getSupportType() {
        return FormatterEnum.PERCENT;
    }
}

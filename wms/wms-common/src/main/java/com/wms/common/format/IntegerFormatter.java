package com.wms.common.format;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class IntegerFormatter extends FormatSupport<Object> {
    @Override
    protected String doFormat(Object rawValue) {
        // 创建一个DecimalFormat实例，并指定格式化模式
        DecimalFormat formatter = new DecimalFormat("0");
        // 使用format方法进行格式化
        return formatter.format(rawValue);
    }

    @Override
    public FormatterEnum getSupportType() {
        return FormatterEnum.INTEGER;
    }
}

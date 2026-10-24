package com.wms.common.format;

import cn.hutool.core.date.LocalDateTimeUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeFormatter extends FormatSupport<LocalDateTime> {
    private static final String dateFormatPatten = "yyyy-MM-dd hh:mm:ss";

    @Override
    public FormatterEnum getSupportType() {
        return FormatterEnum.DATE_TIME;
    }

    @Override
    protected String doFormat(LocalDateTime rawValue) {
        return LocalDateTimeUtil.format(rawValue, dateFormatPatten);
    }
}

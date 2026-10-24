package com.wms.common.format;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeShortFormatter extends FormatSupport<LocalDateTime> {
    private static final String dateTimeFormatPatten = "yy/MM/dd HH:mm";

    @Override
    protected String doFormat(LocalDateTime rawValue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormatPatten);
        return rawValue.format(formatter);
    }

    @Override
    public FormatterEnum getSupportType() {
        return FormatterEnum.DATE_TIME_SHORT;
    }
}

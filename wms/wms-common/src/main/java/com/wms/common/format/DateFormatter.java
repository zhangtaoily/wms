package com.wms.common.format;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateFormatter extends FormatSupport<Object> {
    private static final String dateFormatPatten = "yyyy-MM-dd";
    private static final String dateTimeFormatPatten = "yyyy-MM-dd HH:mm:ss";

    @Override
    public FormatterEnum getSupportType() {
        return FormatterEnum.DATE;
    }

    @Override
    protected String doFormat(Object rawValue) {
        if (rawValue instanceof LocalDate) {
            return String.valueOf(rawValue);
        }

        if (rawValue instanceof LocalDateTime) {
            return String.valueOf(((LocalDateTime) rawValue).toLocalDate());
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(String.valueOf(rawValue), DateTimeFormatter.ofPattern(dateTimeFormatPatten));
            LocalDate localDate = dateTime.toLocalDate();
            return String.valueOf(localDate);
        } catch (Exception e) {
            LocalDate dateTime = LocalDate.parse(String.valueOf(rawValue), DateTimeFormatter.ofPattern(dateFormatPatten));
            return String.valueOf(dateTime);
        }
    }
}

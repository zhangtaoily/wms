package com.wms.common.format;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateShortFormatter extends FormatSupport<Object> {
    private static final String dateShortFormatPatten = "yy/MM/dd";
    private static final String dateFormatPatten = "yyyy-MM-dd";
    private static final String dateTimeFormatPatten = "yyyy-MM-dd HH:mm:ss";

    @Override
    protected String doFormat(Object rawValue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateShortFormatPatten);

        if (rawValue instanceof LocalDate) {
            return ((LocalDate) rawValue).format(formatter);
        }

        if (rawValue instanceof LocalDateTime) {
            return ((LocalDateTime) rawValue).format(formatter);
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(String.valueOf(rawValue), DateTimeFormatter.ofPattern(dateTimeFormatPatten));
            LocalDate localDate = dateTime.toLocalDate();
            return localDate.format(formatter);
        } catch (Exception e) {
            LocalDate dateTime = LocalDate.parse(String.valueOf(rawValue), DateTimeFormatter.ofPattern(dateFormatPatten));
            return dateTime.format(formatter);
        }
    }

    @Override
    public FormatterEnum getSupportType() {
        return FormatterEnum.DATE_SHORT;
    }
}

package com.wms.common.format;

import com.wms.common.utils.ListUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public enum FormatterEnum {

    DATE("yyyy-MM-dd", DateFormatter.class, "格式化为日期"),
    TIME("HH:mm:ss", TimeFormatter.class, "格式化为时间"),
    DATE_TIME("yyyy-MM-dd HH:mm:ss", DateTimeFormatter.class, "格式化为日期时间"),
    DATE_TIME_SHORT("yy/MM/dd HH:mm", DateTimeShortFormatter.class, "格式化为没有秒的两位年的日期时间"),
    DATE_SHORT("yy/MM/dd", DateTimeShortFormatter.class, "格式化为斜杠的年月日"),
    ONE_DECIMAL("#,###.#", OneDecimalFormatter.class, "格式化一位小数"),
    TWO_DECIMAL("#,###.##", TwoDecimalFormatter.class, "格式化为两位小数"),
    THREE_DECIMAL("#,###.###", FourDecimalFormatter.class, "格式化为三位小数"),
    FOUR_DECIMAL("#,###.####", FourDecimalFormatter.class, "格式化为四位小数"),
    SIX_DECIMAL("#,###.######", SixDecimalFormatter.class, "格式化为六位小数"),
    PERCENT("##.##%", PercentFormatter.class, "格式化为两位小数的百分比"),
    PERCENT_INT("##%", PercentFormatter.class, "格式化为没有小数的百分比"),
    INTEGER("#,###", IntegerFormatter.class, "格式化为整数");

    private final String format;
    private final Class<?> cla;
    private final String desc;

    public static Map<String, FormatterEnum> formatorEnumMap = ListUtils.toMap(ListUtils.adaptArray(values()), FormatterEnum::getFormat);

}

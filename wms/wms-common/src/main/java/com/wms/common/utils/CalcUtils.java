package com.wms.common.utils;

import lombok.experimental.UtilityClass;

/**
 * @author li shuang
 */
@UtilityClass
public class CalcUtils {
    // 计算进度百分比
    public static int calculateProgress(int processed, int total) {
        return (int) ((processed * 100.0) / total);
    }

}

package com.wms.common.utils;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

/**
 * 表格信息
 * @author li shuang
 */
public class TableInfoUtils {
    /**
     * 获取表名
     * @param clazz
     * @return
     */
    public static String getTableName(Class<?> clazz){
        return TableInfoHelper.getTableInfo(clazz).getTableName();
    }
    /**
     * 获取主键列
     * @param clazz
     * @return
     */
    public static String getKeyColumn(Class<?> clazz){
        return TableInfoHelper.getTableInfo(clazz).getKeyColumn();
    }
}

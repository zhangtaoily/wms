package com.wms.service.common.utils;

import com.wms.common.core.domain.CancelClosedBaseEntity;

/**
 * TableNodeHelper 类包含了一些常用的查询字段和连接表逻辑，用于减少代码冗余和提高代码的可维护性。
 *
 * @author li shuang
 */
public class TableNodeHelper {


    /**
     * 获取常用的查询字段，包括 createUser、updateUser 的 nick_name 字段。
     *
     * @return 查询字段字符串
     */
    public static String getCommonJoinColumnsWithCheck(Class<?> clazz) {
        String commonJoinColumns = getCommonJoinColumns(clazz);
        return commonJoinColumns + ", checkUser.nick_name checkName";
    }

    /**
     * 获取常用的连接表逻辑，包括 createUser、updateUser 的连接表语句。
     *
     * @return 连接表语句字符串
     */
    public static String getCommonJoinTablesWithCheck(Class<?> clazz ,String tableName) {
        String commonJoinTables = getCommonJoinTables(clazz, tableName);
        return commonJoinTables + "  left join sys_user checkUser on checkUser.id = "+ tableName + ".check_by";
    }

    /**
     * 获取常用的查询字段，包括 createUser、updateUser 的 nick_name 字段。
     *
     * @return 查询字段字符串
     */
    public static String getCommonJoinColumns(Class<?> clazz) {
        if(CancelClosedBaseEntity.class.isAssignableFrom(clazz)){
            return " closedUser.nick_name closedName, cancelUser.nick_name cancelName," + getCommonJoinColumns();
        }
        return getCommonJoinColumns();
    }

    /**
     * 获取常用的连接表逻辑，包括 createUser、updateUser 的连接表语句。
     *
     * @return 连接表语句字符串
     */
    public static String getCommonJoinTables(Class<?> clazz ,String tableName) {
        if(CancelClosedBaseEntity.class.isAssignableFrom(clazz)){
            return " left join sys_user closedUser on closedUser.id = " + tableName + ".closed_by" +
                " left join sys_user cancelUser on cancelUser.id = " + tableName + ".cancel_by " + getCommonJoinTables(tableName);
        }
        return getCommonJoinTables(tableName);
    }

    /**
     * 获取常用的查询字段，包括 createUser、updateUser 的 nick_name 字段。
     *
     * @return 查询字段字符串
     */
    public static String getCommonJoinColumns() {
        return " createUser.nick_name createName, updateUser.nick_name updateName, auditUser.nick_name auditName";
    }

    /**
     * 获取常用的连接表逻辑，包括 createUser、updateUser 的连接表语句。
     *
     * @return 连接表语句字符串
     */
    public static String getCommonJoinTables(String tableName) {
        return " left join sys_user createUser on createUser.id = " + tableName + ".create_by" +
            " left join sys_user updateUser on updateUser.id = " + tableName + ".update_by" +
            " left join sys_user auditUser on auditUser.id = " + tableName + ".auditor";
    }

    /**
     * 获取常用的查询字段，包括 createUser、updateUser 的 nick_name 字段。
     * 没有审核人
     * @return 查询字段字符串
     */
    public static String getCommonJoinColumnsNotAudit() {
        return " createUser.nick_name createName, updateUser.nick_name updateName";
    }

    /**
     * 获取常用的连接表逻辑，包括 createUser、updateUser 的连接表语句。
     * 没有审核人
     * @return 连接表语句字符串
     */
    public static String getCommonJoinTablesNotAudit(String tableName) {
        return " left join sys_user createUser on createUser.id = " + tableName + ".create_by" +
            " left join sys_user updateUser on updateUser.id = " + tableName + ".update_by";
    }
}


package com.wms.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.wms.common.core.domain.Entity;
import com.wms.common.core.domain.IdEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CustomizeMapper<T extends Entity> extends BaseMapper<T> {

    Map<String, Class<?>> CLAZZ_MAP = new HashMap<>();

    @Select("${sql}")
    Long count(@Param("sql") String sql);

    @Select("${sql}")
    BigDecimal sum(@Param("sql") String sql);

    @Update("${sql}")
    void execute(@Param("sql") String sql);

    @Select("${sql}")
    Map<String, Object> query(@Param("sql") String sql);

    @Select("${sql}")
    List<Map<String, Object>> queryList(@Param("sql") String sql);

    /**
     * 条件树参数化查询：${sql} 先替换，替换文本中的 #{ctP.pN} 再由
     * SqlSourceBuilder 二次解析完成参数绑定（纯 Map 键跳转，同 ${ew.customSqlSegment} 机制）
     */
    @Select("${sql}")
    List<Map<String, Object>> queryList(@Param("sql") String sql, @Param("ctP") Map<String, Object> ctP);

    @Select("${sql}")
    Long count(@Param("sql") String sql, @Param("ctP") Map<String, Object> ctP);

    @SuppressWarnings("unchecked")
    default Class<T> supportEntityType() {
        return (Class<T>) CLAZZ_MAP.computeIfAbsent(
            getClass().getName(),
            x -> ReflectionKit.getSuperClassGenericType(getClass(), CustomizeMapper.class, 0));
    }

    default boolean disableCache() {
        return false;
    }

    @Select("SELECT id FROM ${tableName} ${ew.customSqlSegment}")
    List<T> selectIds(@Param("tableName") String tableName, @Param(Constants.WRAPPER) Wrapper<T> wrapper);

    /**
     * 自由查询
     *
     * @param selectSql 查询sql
     * @param tableName 表名
     * @param wrapper   条件
     * @return ListMap 结果
     */
    @Select("SELECT ${selectSql} FROM ${tableName} ${ew.customSqlSegment}")
    List<Map<String, Object>> select(@Param("selectSql") String selectSql,@Param("tableName") String tableName, @Param(Constants.WRAPPER) Wrapper<T> wrapper);

    /**
     * 清空表
     *
     * @param tableName 表名
     */
    @Update("truncate table ${tableName}")
    void truncate(String tableName);

    /**
     * 清空表数据 (可被事务回滚)
     *
     * @param tableName 表名
     */
    @Delete("DELETE FROM ${tableName}")
    void deleteTableData(String tableName);
}

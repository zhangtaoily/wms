package com.wms.service.common.service;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.wms.common.utils.ListUtils;
import com.wms.service.common.meta.LambdaFieldUtils;

import java.util.function.Function;

/**
 * TableNode构建器类，提供便捷的配置方式
 */
public class TableNodeBuilder {

    /**
     * JOIN条件类，用于封装JOIN条件
     */
    public static class Join {
        private final String condition;

        private Join(String condition) {
            this.condition = condition;
        }

        /**
         * 创建JOIN条件（使用SQL字符串）
         *
         * @param condition 条件字符串
         * @return Join
         */
        public static Join on(String condition) {
            return new Join(condition);
        }

        /**
         * 创建JOIN条件（使用lambda表达式）
         *
         * @param mainAlias 主表别名
         * @param mainField 主表字段
         * @param subAlias 子表别名
         * @param subField 子表字段
         * @param <T1> 主表类型
         * @param <T2> 子表类型
         * @return Join
         */
        public static <T1, T2> Join on(String mainAlias, SFunction<T1, ?> mainField,
                                      String subAlias, SFunction<T2, ?> subField) {
            String mainColumn = LambdaFieldUtils.getColumnName(mainField);
            String subColumn = LambdaFieldUtils.getColumnName(subField);
            String condition = String.format("%s.%s = %s.%s", mainAlias, mainColumn, subAlias, subColumn);
            return new Join(condition);
        }

        /**
         * 创建JOIN条件（使用lambda表达式和额外条件）
         *
         * @param mainAlias 主表别名
         * @param mainField 主表字段
         * @param subAlias 子表别名
         * @param subField 子表字段
         * @param extraCondition 额外条件
         * @param <T1> 主表类型
         * @param <T2> 子表类型
         * @return Join
         */
        public static <T1, T2> Join on(String mainAlias, SFunction<T1, ?> mainField,
                                      String subAlias, SFunction<T2, ?> subField,
                                      String extraCondition) {
            String mainColumn = LambdaFieldUtils.getColumnName(mainField);
            String subColumn = LambdaFieldUtils.getColumnName(subField);
            String condition = String.format("%s.%s = %s.%s", mainAlias, mainColumn, subAlias, subColumn);
            if (extraCondition != null && !extraCondition.trim().isEmpty()) {
                condition += " AND " + extraCondition;
            }
            return new Join(condition);
        }

        /**
         * 创建JOIN条件（使用条件构建器）
         *
         * @param conditionBuilder 条件构建器
         * @return Join
         */
        public static Join on(ConditionBuilder conditionBuilder) {
            return new Join(conditionBuilder.build());
        }

        /**
         * 获取条件字符串
         *
         * @return 条件字符串
         */
        public String getCondition() {
            return condition;
        }
    }

    /**
     * 条件构建器类，用于构建复杂的JOIN条件
     */
    public static class ConditionBuilder {
        private final StringBuilder condition = new StringBuilder();

        /**
         * 添加相等条件
         *
         * @param alias 表别名
         * @param field 字段
         * @param value 值
         * @param <T> 实体类型
         * @return ConditionBuilder
         */
        public <T> ConditionBuilder eq(String alias, SFunction<T, ?> field, Object value) {
            String column = LambdaFieldUtils.getColumnName(field);
            if (condition.length() > 0) {
                condition.append(" AND ");
            }
            if (value instanceof String) {
                condition.append(String.format("%s.%s = '%s'", alias, column, value));
            } else {
                condition.append(String.format("%s.%s = %s", alias, column, value));
            }
            return this;
        }

        /**
         * 添加字段相等条件
         *
         * @param alias1 表1别名
         * @param field1 表1字段
         * @param alias2 表2别名
         * @param field2 表2字段
         * @param <T1> 表1类型
         * @param <T2> 表2类型
         * @return ConditionBuilder
         */
        public <T1, T2> ConditionBuilder eq(String alias1, SFunction<T1, ?> field1,
                                           String alias2, SFunction<T2, ?> field2) {
            String column1 = LambdaFieldUtils.getColumnName(field1);
            String column2 = LambdaFieldUtils.getColumnName(field2);
            if (condition.length() > 0) {
                condition.append(" AND ");
            }
            condition.append(String.format("%s.%s = %s.%s", alias1, column1, alias2, column2));
            return this;
        }

        /**
         * 添加常量值相等条件
         *
         * @param alias 表别名
         * @param field 字段
         * @param value 常量值
         * @param <T> 实体类型
         * @return ConditionBuilder
         */
        public <T> ConditionBuilder eqValue(String alias, SFunction<T, ?> field, Object value) {
            String column = LambdaFieldUtils.getColumnName(field);
            if (condition.length() > 0) {
                condition.append(" AND ");
            }
            if (value instanceof String) {
                condition.append(String.format("%s.%s = '%s'", alias, column, value));
            } else {
                condition.append(String.format("%s.%s = %s", alias, column, value));
            }
            return this;
        }

        /**
         * 添加相等条件（从实体类获取表名）
         *
         * @param tableClass 实体类
         * @param field 字段
         * @param value 值
         * @param <T> 实体类型
         * @return ConditionBuilder
         */
        public <T> ConditionBuilder eq(Class<T> tableClass, SFunction<T, ?> field, Object value) {
            String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
            String column = LambdaFieldUtils.getColumnName(field);
            if (condition.length() > 0) {
                condition.append(" AND ");
            }
            if (value instanceof String) {
                condition.append(String.format("%s.%s = '%s'", tableName, column, value));
            } else {
                condition.append(String.format("%s.%s = %s", tableName, column, value));
            }
            return this;
        }

        /**
         * 添加字段相等条件（从实体类获取表名）
         *
         * @param tableClass1 表1实体类
         * @param field1 表1字段
         * @param tableClass2 表2实体类
         * @param field2 表2字段
         * @param <T1> 表1类型
         * @param <T2> 表2类型
         * @return ConditionBuilder
         */
        public <T1, T2> ConditionBuilder eq(Class<T1> tableClass1, SFunction<T1, ?> field1,
                                           Class<T2> tableClass2, SFunction<T2, ?> field2) {
            String tableName1 = TableInfoHelper.getTableInfo(tableClass1).getTableName();
            String tableName2 = TableInfoHelper.getTableInfo(tableClass2).getTableName();
            String column1 = LambdaFieldUtils.getColumnName(field1);
            String column2 = LambdaFieldUtils.getColumnName(field2);
            if (condition.length() > 0) {
                condition.append(" AND ");
            }
            condition.append(String.format("%s.%s = %s.%s", tableName1, column1, tableName2, column2));
            return this;
        }

        /**
         * 添加常量值相等条件（从实体类获取表名）
         *
         * @param tableClass 实体类
         * @param field 字段
         * @param value 值
         * @param <T> 实体类型
         * @return ConditionBuilder
         */
        public <T> ConditionBuilder eqValue(Class<T> tableClass, SFunction<T, ?> field, Object value) {
            String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
            String column = LambdaFieldUtils.getColumnName(field);
            if (condition.length() > 0) {
                condition.append(" AND ");
            }
            if (value instanceof String) {
                condition.append(String.format("%s.%s = '%s'", tableName, column, value));
            } else {
                condition.append(String.format("%s.%s = %s", tableName, column, value));
            }
            return this;
        }

        /**
         * 构建条件字符串
         *
         * @return 条件字符串
         */
        public String build() {
            return condition.toString();
        }
    }

    /**
     * 创建条件构建器
     *
     * @return ConditionBuilder
     */
    public static ConditionBuilder condition() {
        return new ConditionBuilder();
    }

    private final EnhancedTableNode tableNode;

    private TableNodeBuilder(Class<?> tableClass) {
        this.tableNode = new EnhancedTableNode();
        this.tableNode.setTableClass(tableClass);
        this.tableNode.setTableName(TableInfoHelper.getTableInfo(tableClass).getTableName());
        this.tableNode.setPrimaryKey(TableInfoHelper.getTableInfo(tableClass).getKeyColumn());
    }

    /**
     * 创建构建器
     */
    public static TableNodeBuilder create(Class<?> tableClass) {
        return new TableNodeBuilder(tableClass);
    }

    /**
     * 设置Mapper类
     */
    public TableNodeBuilder mapper(Class<?> mapperClass) {
        tableNode.setMapper(mapperClass);
        return this;
    }

    /**
     * 设置父表
     */
    public TableNodeBuilder parentTable(String parentTable) {
        tableNode.setParentTable(parentTable);
        return this;
    }

    /**
     * 设置外键
     */
    public TableNodeBuilder foreignKey(String foreignKey) {
        tableNode.setForeignKey(foreignKey);
        return this;
    }

    /**
     * 设置是否为明细表
     */
    public TableNodeBuilder isGoods(boolean isGoods) {
        tableNode.setIsGoods(isGoods);
        return this;
    }

    /**
     * 定义JOIN条件（支持lambda表达式）
     * <p>
     * 用于定义主表与子表的关联关系，通过lambda表达式指定关联字段。
     * 必须在leftJoin、innerJoin、rightJoin之后调用，用于替换写死的join条件。
     * </p>
     *
     * @param mainTable 主表实体类
     * @param mainField 主表关联字段
     * @param subTable 子表实体类
     * @param subField 子表关联字段
     * @param <T1> 主表类型
     * @param <T2> 子表类型
     * @return TableNodeBuilder
     */
    public <T1, T2> TableNodeBuilder on(Class<T1> mainTable, SFunction<T1, ?> mainField,
                                        Class<T2> subTable, SFunction<T2, ?> subField) {
        String mainTableName = TableInfoHelper.getTableInfo(mainTable).getTableName();
        String mainColumn = LambdaFieldUtils.getColumnName(mainField);

        String subTableName = TableInfoHelper.getTableInfo(subTable).getTableName();
        String subColumn = LambdaFieldUtils.getColumnName(subField);

        String joinCondition = String.format("%s.%s = %s.%s",
            mainTableName, mainColumn, subTableName, subColumn);

        // 修改最近添加的JoinTable的条件
        if (!tableNode.getJoinTableList().isEmpty()) {
            EnhancedTableNode.JoinTable lastJoinTable = tableNode.getJoinTableList().get(tableNode.getJoinTableList().size() - 1);
            lastJoinTable.setJoinCondition(joinCondition);
        }

        return this;
    }

    /**
     * 定义JOIN条件（支持lambda表达式和表别名）
     * <p>
     * 用于定义主表与子表的关联关系，通过lambda表达式指定关联字段，并支持指定表别名。
     * 必须在leftJoin、innerJoin、rightJoin之后调用，用于替换写死的join条件。
     * </p>
     *
     * @param mainAlias 主表别名
     * @param mainField 主表关联字段
     * @param subAlias 子表别名
     * @param subField 子表关联字段
     * @param <T1> 主表类型
     * @param <T2> 子表类型
     * @return TableNodeBuilder
     */
    public <T1, T2> TableNodeBuilder on(String mainAlias, SFunction<T1, ?> mainField,
                                        String subAlias, SFunction<T2, ?> subField) {
        String mainColumn = LambdaFieldUtils.getColumnName(mainField);
        String subColumn = LambdaFieldUtils.getColumnName(subField);

        String joinCondition = String.format("%s.%s = %s.%s",
            mainAlias, mainColumn, subAlias, subColumn);

        // 修改最近添加的JoinTable的条件
        if (!tableNode.getJoinTableList().isEmpty()) {
            EnhancedTableNode.JoinTable lastJoinTable = tableNode.getJoinTableList().get(tableNode.getJoinTableList().size() - 1);
            lastJoinTable.setJoinCondition(joinCondition);
        }

        return this;
    }

    /**
     * 定义JOIN条件（主表使用实体类，子表使用别名）
     * <p>
     * 用于定义主表与子表的关联关系，主表通过实体类自动获取表名，子表使用别名。
     * 必须在leftJoin、innerJoin、rightJoin之后调用，用于替换写死的join条件。
     * </p>
     *
     * @param mainTable 主表实体类
     * @param mainField 主表关联字段
     * @param subAlias 子表别名
     * @param subField 子表关联字段
     * @param <T1> 主表类型
     * @param <T2> 子表类型
     * @return TableNodeBuilder
     */
    public <T1, T2> TableNodeBuilder on(Class<T1> mainTable, SFunction<T1, ?> mainField,
                                        String subAlias, SFunction<T2, ?> subField) {
        String mainTableName = TableInfoHelper.getTableInfo(mainTable).getTableName();
        String mainColumn = LambdaFieldUtils.getColumnName(mainField);
        String subColumn = LambdaFieldUtils.getColumnName(subField);

        String joinCondition = String.format("%s.%s = %s.%s",
            mainTableName, mainColumn, subAlias, subColumn);

        // 修改最近添加的JoinTable的条件
        if (!tableNode.getJoinTableList().isEmpty()) {
            EnhancedTableNode.JoinTable lastJoinTable = tableNode.getJoinTableList().get(tableNode.getJoinTableList().size() - 1);
            lastJoinTable.setJoinCondition(joinCondition);
        }

        return this;
    }

    /**
     * 定义JOIN条件（主表使用别名，子表使用实体类）
     * <p>
     * 用于定义主表与子表的关联关系，主表使用别名，子表通过实体类自动获取表名。
     * 必须在leftJoin、innerJoin、rightJoin之后调用，用于替换写死的join条件。
     * </p>
     *
     * @param mainAlias 主表别名
     * @param mainField 主表关联字段
     * @param subTable 子表实体类
     * @param subField 子表关联字段
     * @param <T1> 主表类型
     * @param <T2> 子表类型
     * @return TableNodeBuilder
     */
    public <T1, T2> TableNodeBuilder on(String mainAlias, SFunction<T1, ?> mainField,
                                        Class<T2> subTable, SFunction<T2, ?> subField) {
        String mainColumn = LambdaFieldUtils.getColumnName(mainField);
        String subTableName = TableInfoHelper.getTableInfo(subTable).getTableName();
        String subColumn = LambdaFieldUtils.getColumnName(subField);

        String joinCondition = String.format("%s.%s = %s.%s",
            mainAlias, mainColumn, subTableName, subColumn);

        // 修改最近添加的JoinTable的条件
        if (!tableNode.getJoinTableList().isEmpty()) {
            EnhancedTableNode.JoinTable lastJoinTable = tableNode.getJoinTableList().get(tableNode.getJoinTableList().size() - 1);
            lastJoinTable.setJoinCondition(joinCondition);
        }

        return this;
    }

    /**
     * 添加LEFT JOIN
     */
    public TableNodeBuilder leftJoin(String tableName, String alias) {
        tableNode.addJoinTable(EnhancedTableNode.JoinType.LEFT_JOIN, tableName, alias);
        return this;
    }

    /**
     * 添加LEFT JOIN（alias默认为表名）
     */
    public TableNodeBuilder leftJoin(String tableName) {
        tableNode.addJoinTable(EnhancedTableNode.JoinType.LEFT_JOIN, tableName, tableName);
        return this;
    }

    /**
     * 添加LEFT JOIN（支持Class参数）
     */
    public TableNodeBuilder leftJoin(Class<?> tableClass, String alias) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.LEFT_JOIN, tableName, alias);
        return this;
    }

    /**
     * 添加LEFT JOIN（支持Class参数，无joinCondition，后续通过on方法设置）
     */
    public TableNodeBuilder leftJoin(Class<?> tableClass) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.LEFT_JOIN, tableName, tableName);
        return this;
    }

    /**
     * 添加LEFT JOIN（支持Class参数和Join对象）
     * <p>
     * 用于定义主表与子表的关联关系，并通过Join对象设置JOIN条件。
     * </p>
     *
     * @param tableClass 子表实体类
     * @param alias 子表别名
     * @param join JOIN条件对象
     * @return TableNodeBuilder
     */
    public TableNodeBuilder leftJoin(Class<?> tableClass, String alias, Join join) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.LEFT_JOIN, tableName, alias, join.getCondition());
        return this;
    }

    /**
     * 添加LEFT JOIN（支持Class参数和Join对象，alias默认为表名）
     * <p>
     * 用于定义主表与子表的关联关系，并通过Join对象设置JOIN条件。
     * </p>
     *
     * @param tableClass 子表实体类
     * @param join JOIN条件对象
     * @return TableNodeBuilder
     */
    public TableNodeBuilder leftJoin(Class<?> tableClass, Join join) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.LEFT_JOIN, tableName, tableName, join.getCondition());
        return this;
    }

    /**
     * 添加INNER JOIN
     */
    public TableNodeBuilder innerJoin(String tableName, String alias) {
        tableNode.addJoinTable(EnhancedTableNode.JoinType.INNER_JOIN, tableName, alias);
        return this;
    }

    /**
     * 添加INNER JOIN（alias默认为表名）
     */
    public TableNodeBuilder innerJoin(String tableName) {
        tableNode.addJoinTable(EnhancedTableNode.JoinType.INNER_JOIN, tableName, tableName);
        return this;
    }

    /**
     * 添加INNER JOIN（支持Class参数）
     */
    public TableNodeBuilder innerJoin(Class<?> tableClass, String alias) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.INNER_JOIN, tableName, alias);
        return this;
    }

    /**
     * 添加INNER JOIN（支持Class参数，alias默认为表名）
     */
    public TableNodeBuilder innerJoin(Class<?> tableClass) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.INNER_JOIN, tableName, tableName);
        return this;
    }

    /**
     * 添加INNER JOIN（支持Class参数和Join对象）
     * <p>
     * 用于定义主表与子表的关联关系，并通过Join对象设置JOIN条件。
     * </p>
     *
     * @param tableClass 子表实体类
     * @param alias 子表别名
     * @param join JOIN条件对象
     * @return TableNodeBuilder
     */
    public TableNodeBuilder innerJoin(Class<?> tableClass, String alias, Join join) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.INNER_JOIN, tableName, alias, join.getCondition());
        return this;
    }

    /**
     * 添加INNER JOIN（支持Class参数和Join对象，alias默认为表名）
     * <p>
     * 用于定义主表与子表的关联关系，并通过Join对象设置JOIN条件。
     * </p>
     *
     * @param tableClass 子表实体类
     * @param join JOIN条件对象
     * @return TableNodeBuilder
     */
    public TableNodeBuilder innerJoin(Class<?> tableClass, Join join) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.INNER_JOIN, tableName, tableName, join.getCondition());
        return this;
    }

    /**
     * 添加RIGHT JOIN
     */
    public TableNodeBuilder rightJoin(String tableName, String alias) {
        tableNode.addJoinTable(EnhancedTableNode.JoinType.RIGHT_JOIN, tableName, alias);
        return this;
    }

    /**
     * 添加RIGHT JOIN（alias默认为表名）
     */
    public TableNodeBuilder rightJoin(String tableName) {
        tableNode.addJoinTable(EnhancedTableNode.JoinType.RIGHT_JOIN, tableName, tableName);
        return this;
    }

    /**
     * 添加RIGHT JOIN（支持Class参数）
     */
    public TableNodeBuilder rightJoin(Class<?> tableClass, String alias) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.RIGHT_JOIN, tableName, alias);
        return this;
    }

    /**
     * 添加RIGHT JOIN（支持Class参数，alias默认为表名）
     */
    public TableNodeBuilder rightJoin(Class<?> tableClass) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.RIGHT_JOIN, tableName, tableName);
        return this;
    }

    /**
     * 添加RIGHT JOIN（支持Class参数和Join对象）
     * <p>
     * 用于定义主表与子表的关联关系，并通过Join对象设置JOIN条件。
     * </p>
     *
     * @param tableClass 子表实体类
     * @param alias 子表别名
     * @param join JOIN条件对象
     * @return TableNodeBuilder
     */
    public TableNodeBuilder rightJoin(Class<?> tableClass, String alias, Join join) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.RIGHT_JOIN, tableName, alias, join.getCondition());
        return this;
    }

    /**
     * 添加RIGHT JOIN（支持Class参数和Join对象，alias默认为表名）
     * <p>
     * 用于定义主表与子表的关联关系，并通过Join对象设置JOIN条件。
     * </p>
     *
     * @param tableClass 子表实体类
     * @param join JOIN条件对象
     * @return TableNodeBuilder
     */
    public TableNodeBuilder rightJoin(Class<?> tableClass, Join join) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinTable(EnhancedTableNode.JoinType.RIGHT_JOIN, tableName, tableName, join.getCondition());
        return this;
    }

    /**
     * 添加原始SQL JOIN（兼容现有代码）
     */
    public TableNodeBuilder rawJoin(String rawSql) {
        tableNode.addRawJoinTable(rawSql);
        return this;
    }

    /**
     * 添加连接列
     */
    public TableNodeBuilder joinColumn(String table, String column, String alias) {
        tableNode.addJoinColumn(table, column, alias);
        return this;
    }

    /**
     * 添加连接列（alias默认为列名）
     */
    public TableNodeBuilder joinColumn(String table, String column) {
        tableNode.addJoinColumn(table, column, column);
        return this;
    }

    /**
     * 添加连接列（支持Class参数）
     */
    public TableNodeBuilder joinColumn(Class<?> tableClass, String column, String alias) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinColumn(tableName, column, alias);
        return this;
    }

    /**
     * 添加连接列（支持Class参数，alias默认为列名）
     */
    public TableNodeBuilder joinColumn(Class<?> tableClass, String column) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addJoinColumn(tableName, column, column);
        return this;
    }

    /**
     * 添加连接列（支持lambda表达式）
     */
    public <T> TableNodeBuilder joinColumn(Class<T> tableClass, SFunction<T, ?> columnFn) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        String column = getColumnName(columnFn);
        tableNode.addJoinColumn(tableName, column, column);
        return this;
    }

    /**
     * 添加连接列（支持lambda表达式，带alias）
     */
    public <T> TableNodeBuilder joinColumn(Class<T> tableClass, SFunction<T, ?> columnFn, String alias) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        String column = getColumnName(columnFn);
        tableNode.addJoinColumn(tableName, column, alias);
        return this;
    }

    /**
     * 添加原始SQL连接列（兼容现有代码）
     */
    public TableNodeBuilder rawJoinColumn(String rawSql) {
        tableNode.addRawJoinColumn(rawSql);
        return this;
    }

    /**
     * 添加排序
     */
    public TableNodeBuilder orderBy(String table, String column, EnhancedTableNode.OrderDirection direction) {
        tableNode.addOrderBy(table, column, direction);
        return this;
    }

    /**
     * 添加排序（支持Class参数）
     */
    public TableNodeBuilder orderBy(Class<?> tableClass, String column, EnhancedTableNode.OrderDirection direction) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        tableNode.addOrderBy(tableName, column, direction);
        return this;
    }

    /**
     * 添加排序（支持lambda表达式）
     */
    public <T> TableNodeBuilder orderBy(Class<T> tableClass, Function<T, ?> columnFn, EnhancedTableNode.OrderDirection direction) {
        String tableName = TableInfoHelper.getTableInfo(tableClass).getTableName();
        String column = getColumnName(columnFn);
        tableNode.addOrderBy(tableName, column, direction);
        return this;
    }

    /**
     * 添加原始SQL排序（兼容现有代码）
     */
    public TableNodeBuilder rawOrderBy(String rawSql) {
        tableNode.addRawOrderBy(rawSql);
        return this;
    }

    /**
     * 添加子表
     */
    public TableNodeBuilder addChild(EnhancedTableNode child) {
        if (tableNode.getChildren() == null) {
            tableNode.setChildren(ListUtils.of(child));
        } else {
            tableNode.getChildren().add(child);
        }
        return this;
    }

    /**
     * 构建TableNode
     */
    public EnhancedTableNode build() {
        return tableNode;
    }

    /**
     * 快速构建主表
     */
    public static EnhancedTableNode buildMainTable(Class<?> tableClass, Class<?> mapperClass) {
        return TableNodeBuilder.create(tableClass)
            .mapper(mapperClass)
            .build();
    }

    /**
     * 快速构建子表
     */
    public static EnhancedTableNode buildChildTable(Class<?> tableClass, Class<?> mapperClass,
                                                   String parentTable, String foreignKey) {
        return TableNodeBuilder.create(tableClass)
            .mapper(mapperClass)
            .parentTable(parentTable)
            .foreignKey(foreignKey)
            .build();
    }

    /**
     * 解析实体类字段名（支持lambda表达式）
     * <p>
     * 该方法委托给LambdaFieldUtils.getColumnName处理，实现代码复用。
     * </p>
     */
    public static <T> String getColumnName(Function<T, ?> fn) {
        return LambdaFieldUtils.getColumnName(fn);
    }
}

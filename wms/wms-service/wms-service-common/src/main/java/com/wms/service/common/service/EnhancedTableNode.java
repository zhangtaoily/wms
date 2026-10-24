package com.wms.service.common.service;

import com.wms.common.utils.MapUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 增强版TableNode类，支持纯Java配置替代SQL拼接
 * 提供类型安全的JoinTable、JoinColumn、OrderBy配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnhancedTableNode extends TableNode {

    /**
     * JoinTable配置列表
     */
    private List<JoinTable> joinTableList = new CopyOnWriteArrayList<>();

    /**
     * JoinColumn配置列表
     */
    private List<JoinColumn> joinColumnList = new CopyOnWriteArrayList<>();

    /**
     * OrderBy配置列表
     */
    private List<OrderBy> orderByList = new CopyOnWriteArrayList<>();

    /**
     * JoinTable配置类
     */
    @Data
    public static class JoinTable {
        /**
         * 连接类型：LEFT JOIN, INNER JOIN, RIGHT JOIN
         */
        private JoinType joinType;

        /**
         * 连接的表名
         */
        private String tableName;

        /**
         * 连接别名
         */
        private String alias;

        /**
         * 连接条件
         */
        private String joinCondition;

        /**
         * 是否使用原始SQL（兼容现有代码）
         */
        private boolean useRawSql = false;

        /**
         * 原始SQL（兼容现有代码）
         */
        private String rawSql;

        public JoinTable(JoinType joinType, String tableName, String alias) {
            this.joinType = joinType;
            this.tableName = tableName;
            this.alias = alias;
        }

        public JoinTable(JoinType joinType, String tableName, String alias, String joinCondition) {
            this.joinType = joinType;
            this.tableName = tableName;
            this.alias = alias;
            this.joinCondition = joinCondition;
        }

        public JoinTable(String rawSql) {
            this.useRawSql = true;
            this.rawSql = rawSql;
        }

        @Override
        public String toString() {
            if (useRawSql) {
                return rawSql;
            }
            return String.format("%s %s %s ON %s",
                joinType.getSql(), tableName, alias, joinCondition);
        }
    }

    /**
     * JoinColumn配置类
     */
    @Data
    public static class JoinColumn {
        /**
         * 表名或别名
         */
        private String table;

        /**
         * 列名
         */
        private String column;

        /**
         * 别名
         */
        private String alias;

        /**
         * 是否使用原始SQL（兼容现有代码）
         */
        private boolean useRawSql = false;

        /**
         * 原始SQL（兼容现有代码）
         */
        private String rawSql;

        public JoinColumn(String table, String column, String alias) {
            this.table = table;
            this.column = column;
            this.alias = alias;
        }

        public JoinColumn(String rawSql) {
            this.useRawSql = true;
            this.rawSql = rawSql;
        }

        @Override
        public String toString() {
            if (useRawSql) {
                return rawSql;
            }
            return String.format("%s.%s %s", table, column, alias != null ? "AS " + alias : "");
        }
    }

    /**
     * OrderBy配置类
     */
    @Data
    public static class OrderBy {
        /**
         * 表名或别名
         */
        private String table;

        /**
         * 列名
         */
        private String column;

        /**
         * 排序方向：ASC, DESC
         */
        private OrderDirection direction;

        /**
         * 是否使用原始SQL（兼容现有代码）
         */
        private boolean useRawSql = false;

        /**
         * 原始SQL（兼容现有代码）
         */
        private String rawSql;

        public OrderBy(String table, String column, OrderDirection direction) {
            this.table = table;
            this.column = column;
            this.direction = direction;
        }

        public OrderBy(String rawSql) {
            this.useRawSql = true;
            this.rawSql = rawSql;
        }

        @Override
        public String toString() {
            if (useRawSql) {
                return rawSql;
            }
            return String.format("%s.%s %s", table, column, direction.getSql());
        }
    }

    /**
     * 连接类型枚举
     */
    public enum JoinType {
        LEFT_JOIN("LEFT JOIN"),
        INNER_JOIN("INNER JOIN"),
        RIGHT_JOIN("RIGHT JOIN");

        private final String sql;

        JoinType(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    /**
     * 排序方向枚举
     */
    public enum OrderDirection {
        ASC("ASC"),
        DESC("DESC");

        private final String sql;

        OrderDirection(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    /**
     * 添加JoinTable配置
     */
    public EnhancedTableNode addJoinTable(JoinType joinType, String tableName, String alias) {
        this.joinTableList.add(new JoinTable(joinType, tableName, alias));
        return this;
    }

    /**
     * 添加JoinTable配置（支持joinCondition）
     */
    public EnhancedTableNode addJoinTable(JoinType joinType, String tableName, String alias, String joinCondition) {
        this.joinTableList.add(new JoinTable(joinType, tableName, alias, joinCondition));
        return this;
    }

    /**
     * 添加原始SQL JoinTable（兼容现有代码）
     */
    public EnhancedTableNode addRawJoinTable(String rawSql) {
        this.joinTableList.add(new JoinTable(rawSql));
        return this;
    }

    /**
     * 添加JoinColumn配置
     */
    public EnhancedTableNode addJoinColumn(String table, String column, String alias) {
        this.joinColumnList.add(new JoinColumn(table, column, alias));
        return this;
    }

    /**
     * 添加原始SQL JoinColumn（兼容现有代码）
     */
    public EnhancedTableNode addRawJoinColumn(String rawSql) {
        this.joinColumnList.add(new JoinColumn(rawSql));
        return this;
    }

    /**
     * 添加OrderBy配置
     */
    public EnhancedTableNode addOrderBy(String table, String column, OrderDirection direction) {
        this.orderByList.add(new OrderBy(table, column, direction));
        return this;
    }

    /**
     * 添加原始SQL OrderBy（兼容现有代码）
     */
    public EnhancedTableNode addRawOrderBy(String rawSql) {
        this.orderByList.add(new OrderBy(rawSql));
        return this;
    }

    /**
     * 生成JoinTables SQL
     */
    public String generateJoinTablesSql() {
        if (joinTableList.isEmpty()) {
            return super.getJoinTables();
        }

        StringBuilder sql = new StringBuilder();
        for (JoinTable joinTable : joinTableList) {
            sql.append(" ").append(joinTable.toString());
        }
        return sql.toString();
    }

    /**
     * 生成JoinColumns SQL
     */
    public String generateJoinColumnsSql() {
        if (joinColumnList.isEmpty()) {
            return super.getJoinColumns();
        }

        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < joinColumnList.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append(joinColumnList.get(i).toString());
        }
        return sql.toString();
    }

    /**
     * 生成OrderBy SQL
     */
    public String generateOrderBySql() {
        if (orderByList.isEmpty()) {
            return super.getOrderBy();
        }

        StringBuilder sql = new StringBuilder(" ORDER BY ");
        for (int i = 0; i < orderByList.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(orderByList.get(i).toString());
        }
        return sql.toString();
    }

    /**
     * 重写父类方法，使用生成的SQL
     */
    @Override
    public String getJoinTables() {
        return generateJoinTablesSql();
    }

    @Override
    public String getJoinColumns() {
        return generateJoinColumnsSql();
    }

    @Override
    public String getOrderBy() {
        return generateOrderBySql();
    }

/*    *//**
     * 设置子表
     *//*
    public TableNode setChildren(List<TableNode> children) {
        super.setChildren(children);
        return this;
    }

    *//**
     * 添加子表
     *//*
    public TableNode addChild(TableNode child) {
        if (super.getChildren() == null) {
            super.setChildren(new ArrayList<>());
        }
        super.getChildren().add(child);
        return this;
    }*/
}

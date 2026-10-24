package com.wms.sql;

import com.wms.common.constant.SqlConstant;
import com.wms.common.core.domain.IdEntity;
import com.wms.common.utils.ConditionFunctions;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.Predicates;
import com.wms.common.utils.StringUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataViewMetaData implements SqlElement {
    protected Table<? extends IdEntity> mainTable;

    protected List<Table<? extends IdEntity>> joinTables = ListUtils.empty();

    /**
     * 子视图
     */
    protected List<SubDataViewMetaData> subDataViews = ListUtils.empty();

    /**
     * 选择字段
     */
    protected List<String> columns = ListUtils.empty();
    /**
     * 条件
     */
    protected Where where = new Where();
    /**
     * 额外 WHERE 条件（如 EXISTS 子查询），追加在动态 WHERE 之后
     */
    protected List<String> whereExtras = ListUtils.empty();
    /**
     * 排序
     */
    protected OrderBy orderBy = new OrderBy();
    /**
     * 分组
     */
    protected GroupBy groupBy = new GroupBy();

    protected Limit limit = new Limit();

    protected Offset offset = new Offset();

    protected String count = " count(*) ";

    public <T extends IdEntity> Table<T> table(Class<T> clazz) {
        return table(clazz, null);
    }

    public <T extends IdEntity> Table<T> table(Class<T> clazz, String tableAlias) {
        Table<T> tEntityTable = Table.of(clazz, tableAlias);
        return tEntityTable;
    }

    public SubDataViewMetaData subDataView(DataViewMetaData subDataViewMetaData, String subDataViewAlias) {
        return SubDataViewMetaData.of(subDataViewMetaData, subDataViewAlias);
    }

    public DataViewMetaData from(Table<? extends IdEntity> table) {
        this.mainTable = table;
        return this;
    }

    public DataViewMetaData leftJoin(Table<? extends IdEntity> table, String expr) {
        return join(table, JoinType.LEFT, expr);
    }

    public DataViewMetaData leftJoin(SubDataViewMetaData dataView, String expr) {
        return join(dataView, JoinType.LEFT, expr);
    }

    public DataViewMetaData join(SubDataViewMetaData dataView, JoinType joinType, String expr) {
        dataView.setJoinType(joinType);
        dataView.setJoinExpr(expr);
        subDataViews.add(dataView);
        return this;
    }

    public DataViewMetaData innerJoin(Table<? extends IdEntity> table, String expr) {
        return join(table, JoinType.INNER, expr);
    }

    public DataViewMetaData join(Table<? extends IdEntity> table, JoinType joinType, String expr) {
        table.setJoinType(joinType);
        table.setJoinExpr(expr);
        joinTables.add(table);
        return this;
    }

    public DataViewMetaData count() {
        return this;
    }


    public DataViewMetaData columns(List<String> fields) {
        this.columns.addAll(fields);
        return this;
    }

    /** 已声明的 SELECT 列（主键兜底等行级操作场景判断用） */
    public List<String> fetchColumns() {
        return this.columns;
    }

    /** 主表名（主键兜底列限定表名用，避免 JOIN 查询歧义） */
    public String fetchMainTableName() {
        return this.mainTable == null ? null : this.mainTable.getName();
    }

    /** 主表实体类（逻辑列自动解析按实体元数据判定列是否存在） */
    public Class<?> fetchMainEntity() {
        return this.mainTable == null ? null : this.mainTable.getEntity();
    }

    public DataViewMetaData where(String whereSqlExpr) {
        this.where.setExpr(whereSqlExpr);
        return this;
    }

    /**
     * 追加额外 WHERE 条件（如 EXISTS 子查询），会以 AND 方式拼在动态 WHERE 后面
     */
    public DataViewMetaData whereExtra(String whereExtraExpr) {
        if (this.whereExtras == null || this.whereExtras.isEmpty()) {
            this.whereExtras = new ArrayList<>();
        }
        this.whereExtras.add(whereExtraExpr);
        return this;
    }

    public DataViewMetaData orderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public DataViewMetaData limit(Limit limit) {
        this.limit = limit;
        return this;
    }


    public DataViewMetaData offset(Offset offset) {
        this.offset = offset;
        return this;
    }

    public DataViewMetaData groupBy(List<String> fields) {
        GroupBy groupBy = new GroupBy();
        groupBy.setFields(fields);
        this.setGroupBy(groupBy);
        return this;
    }

    @Override
    public String genSqlExpr() {
        return ListUtils.of(
                SqlConstant.SELECT, ListUtils.makeString(columns, ","),
                SqlConstant.FROM, mainTable.genSqlExpr(),
                ConditionFunctions.funcIfNoneNull(joinTables, x -> ListUtils.collect(x, Table::genSqlExpr).makeString(" ")),
                ConditionFunctions.funcIfNoneNull(subDataViews, x -> ListUtils.collect(x, SubDataViewMetaData::genSqlExpr).makeString(" ")),
                genWhereExpr(),
                ConditionFunctions.funcIfNoneNull(groupBy.genSqlExpr(), expr -> groupBy.genSqlExpr()),
                ConditionFunctions.funcIfNoneNull(orderBy.genSqlExpr(), expr -> orderBy.genSqlExpr()),
                ConditionFunctions.funcIfNoneNull(limit.genSqlExpr(), ex -> limit.genSqlExpr()),
                ConditionFunctions.funcIfNoneNull(offset.genSqlExpr(), ex -> offset.genSqlExpr())
            )
            .reject(Predicates::isNull)
            .makeString(" ");
    }

    @Override
    public String genCountSqlExpr() {
        return ListUtils.of(
                SqlConstant.SELECT, count,
                SqlConstant.FROM, mainTable.genSqlExpr(),
                ConditionFunctions.funcIfNoneNull(joinTables, x -> ListUtils.collect(x, Table::genSqlExpr).makeString(" ")),
                ConditionFunctions.funcIfNoneNull(subDataViews, x -> ListUtils.collect(x, SubDataViewMetaData::genSqlExpr).makeString(" ")),
                genWhereExpr()
            )
            .reject(Predicates::isNull)
            .makeString(" ");
    }

    /**
     * 生成 WHERE 表达式，包含主 WHERE 和额外的 whereExtras
     */
    private String genWhereExpr() {
        boolean hasWhere = StringUtils.isNotEmpty(where.getExpr());
        boolean hasExtra = whereExtras != null && !whereExtras.isEmpty();

        if (!hasWhere && !hasExtra) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (hasWhere) {
            sb.append(where.genSqlExpr());
        } else {
            sb.append(SqlConstant.WHERE);
        }

        if (hasExtra) {
            // 已有 WHERE 则拼 AND，否则 whereExtras 紧跟 WHERE 关键字
            String prefix = hasWhere ? " AND " : " ";
            String extra = String.join(" AND ", whereExtras);
            sb.append(prefix).append(extra);
        }

        return sb.toString();
    }
}

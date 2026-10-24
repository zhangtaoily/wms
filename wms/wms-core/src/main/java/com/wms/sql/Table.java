package com.wms.sql;


import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.constant.SqlConstant;
import com.wms.common.core.domain.IdEntity;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class Table<T extends IdEntity> implements SqlElement {

    private String name;
    private String alias;
    private Class<T> entity;
    private JoinType joinType;
    private String joinExpr;

    public static <T extends IdEntity> Table<T> of(Class<T> clazz) {
        return of(clazz, null);
    }

    public static <T extends IdEntity> Table<T> of(Class<T> clazz, String alias) {
        Table<T> table = new Table<>();
        table.setEntity(clazz);
        table.setName(table.genTableName());
        table.setAlias(alias);
        return table;
    }

    public String genSqlExpr() {
        name = this.genTableName();
        if (joinType == null) {
            return name;
        }

        String tableExpr = name;

        if (StringUtils.isNotEmpty(alias)) {
            tableExpr = name + " as " + alias;
        }

        return ListUtils.makeString(" ", joinType.getExpr(), tableExpr, SqlConstant.ON, joinExpr);
    }

    @Override
    public String genCountSqlExpr() {
        return null;
    }

    public synchronized String genTableName() {
        return TableInfoHelper.getTableInfo(entity).getTableName();
    }
}

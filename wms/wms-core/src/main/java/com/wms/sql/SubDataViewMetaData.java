package com.wms.sql;

import com.wms.common.constant.SqlConstant;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class SubDataViewMetaData extends DataViewMetaData implements SqlElement {
    private JoinType joinType;
    private String joinExpr;
    private String alias;

    public static SubDataViewMetaData of(DataViewMetaData dataViewMetaData, String alias) {
        SubDataViewMetaData subDataView = new SubDataViewMetaData();
        BeanUtils.copyProperties(dataViewMetaData, subDataView);
        subDataView.setAlias(alias);
        return subDataView;
    }

    @Override
    public String genSqlExpr() {

        String tableExpr = " (" + super.genSqlExpr() + ") ";

        if (StringUtils.isNotEmpty(alias)) {
            tableExpr = tableExpr + " as " + alias;
        }

        return ListUtils.makeString(" ", joinType.getExpr(), tableExpr, SqlConstant.ON, joinExpr);
    }

    @Override
    public String genCountSqlExpr() {
        return null;
    }
}

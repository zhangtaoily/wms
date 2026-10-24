package com.wms.common.core.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.enums.FieldTypeEnum;
import com.wms.common.exception.ServiceException;
import com.wms.common.rule.ConditionNode;
import com.wms.common.rule.Operator;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;
import com.wms.common.utils.sql.SqlUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wms.common.rule.Operator.*;

/**
 * 分页查询实体类
 *
 * @author Lion Li
 */

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 当前页数
     */
    private Integer pageNum;

    /**
     * 排序列
     */
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    private String isAsc;

    /**
     * 当前记录起始索引 默认值
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 每页显示记录数 默认值 默认查全部
     */
    public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;

    private String moduleGroup;

    private String functionName;

    private String dialogName;

    private ProcessRowParams processRowParams;

    private List<ConditionItem> conditions = ListUtils.empty();

    /**
     * 条件树 DSL（架构 3.4）；非空时优先生效，conditions（FAST/ADVANCE）保持兼容
     */
    private ConditionNode conditionTree;

    private Map<String, Object> params = new HashMap<>();

    public <T> Page<T> build() {
        Integer pageNum = ObjectUtil.defaultIfNull(getPageNum(), DEFAULT_PAGE_NUM);
        Integer pageSize = ObjectUtil.defaultIfNull(getPageSize(), DEFAULT_PAGE_SIZE);
        if (pageNum <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        Page<T> page = new Page<>(pageNum, pageSize);
        List<OrderItem> orderItems = buildOrderItem();
        if (CollUtil.isNotEmpty(orderItems)) {
            page.addOrder(orderItems);
        }
        return page;
    }

    /**
     * 构建排序
     * <p>
     * 支持的用法如下:
     * {isAsc:"asc",orderByColumn:"id"} order by id asc
     * {isAsc:"asc",orderByColumn:"id,createTime"} order by id asc,create_time asc
     * {isAsc:"desc",orderByColumn:"id,createTime"} order by id desc,create_time desc
     * {isAsc:"asc,desc",orderByColumn:"id,createTime"} order by id asc,create_time desc
     */
    private List<OrderItem> buildOrderItem() {
        if (StringUtils.isBlank(orderByColumn) || StringUtils.isBlank(isAsc)) {
            return null;
        }
        String orderBy = SqlUtil.escapeOrderBySql(orderByColumn);
        orderBy = StringUtils.toUnderScoreCase(orderBy);

        // 兼容前端排序类型
        isAsc = StringUtils.replaceEach(isAsc, new String[]{"ascending", "descending"}, new String[]{"asc", "desc"});

        String[] orderByArr = orderBy.split(StringUtils.SEPARATOR);
        String[] isAscArr = isAsc.split(StringUtils.SEPARATOR);
        if (isAscArr.length != 1 && isAscArr.length != orderByArr.length) {
            throw new ServiceException("排序参数有误");
        }

        List<OrderItem> list = new ArrayList<>();
        // 每个字段各自排序
        for (int i = 0; i < orderByArr.length; i++) {
            String orderByStr = orderByArr[i];
            String isAscStr = isAscArr.length == 1 ? isAscArr[0] : isAscArr[i];
            if ("asc".equals(isAscStr)) {
                list.add(OrderItem.asc(orderByStr));
            } else if ("desc".equals(isAscStr)) {
                list.add(OrderItem.desc(orderByStr));
            } else {
                throw new ServiceException("排序参数有误");
            }
        }
        return list;
    }

    public Integer fetchOffset() {
        return ((this.getPageNum() == null || this.getPageNum() <= 1) ? 0 : (this.getPageNum() - 1) * this.getPageSize());
    }

    @Data
    public static class ConditionItem {
        Operator logicalOperator;
        String field;
        FieldTypeEnum fieldType;
        Operator operate;
        String value;
        FilterGroup filterGroup;

        public String fetchStatement(String column) {
            // 优化后的方式（一行代码）
            return operate.buildStatement(column, value, fieldType);
        }
    }

    public enum FilterGroup {
        FAST,
        ADVANCE;
    }

    public boolean isReadAllWf() {
        return this.getParams().get("requestname") == null ||
            "云梭WMS".equals(this.getParams().get("requestname"));
    }

    //conditions 是否包含指定字段
    public boolean isContainField(String field) {
        return CollUtil.isNotEmpty(conditions) && conditions.stream().anyMatch(conditionItem -> field.equals(conditionItem.getField()));
    }

    //conditions 取指定字段的值
    public String getFieldValue(String field) {
        return CollUtil.isNotEmpty(conditions) ? conditions.stream().filter(conditionItem -> field.equals(conditionItem.getField())).findFirst().get().getValue() : null;
    }
}

package com.wms.common.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 条件树节点（架构规划 3.4 DSL）：叶子与组节点统一建模。
 * <pre>
 * 组节点：{ "logic": "and", "conditions": [ 子节点... ] }
 * 叶子：  { "field": "box_code", "op": "like", "value": "2610%" }
 * </pre>
 *
 * @author wms
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 组节点内子条件的组合方式：and / or，默认 and */
    private String logic = "and";

    /** 组节点子条件列表；非空时本节点按组节点处理 */
    private List<ConditionNode> conditions;

    /** 叶子节点字段名（必须命中字段白名单） */
    private String field;

    /** 叶子节点操作符：eq/ne/gt/ge/lt/le/like/in/between/isNull */
    private String op;

    /** 叶子节点值 */
    private Object value;

    /**
     * 是否组节点
     */
    public boolean isGroup() {
        return conditions != null && !conditions.isEmpty();
    }
}

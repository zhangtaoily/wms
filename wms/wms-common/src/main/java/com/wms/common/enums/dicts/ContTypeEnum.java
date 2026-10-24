package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * * 染色方式数据字典
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ContTypeEnum {
    T001("001", "有正规合同，包括传真合同"),
    T009("009", "正规合同在于客户签订中"),
    T011("011", "合同版本是客户的"),
    T012("012", "技术、开发和市场部下单无合同号"),
    T013("013", "公司无合同号备大货"),
    T014("014", "合同量小于1000KG且无欠款的"),
    T015("015", "合同数量小于300KG的样纱订单"),
    T016("016", "意向合同有正规合同无需订单"),
    T017("017", "意向合同为无产品明细的正规合同需附订单"),
    T018("018", "合同金额小于10W且无欠款的");

    private final String code;
    private final String name;
}

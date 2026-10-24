package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 备纱类型数据字段
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SuYarnTypeEnum {
    E01("01", "绞/筒染备胚纱"),
    E02("02", "非绞/筒染备胚纱"),
    E03("03", "色纱备库存"),
    E04("04", "改染"),
    E05("05", "30KG以下订单不需合同");

    private final String code;
    private final String name;
}

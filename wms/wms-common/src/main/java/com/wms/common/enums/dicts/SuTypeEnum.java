package com.wms.common.enums.dicts;

import com.wms.common.encrypt.encryptor.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 备货类型数据字段
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SuTypeEnum {
    SELL_SPARE_YARN("01", "卖备纱"),
    PRODUCE("02", "生产"),
    RESERVE_STOCK("03", "备库存");

    private final String code;
    private final String name;
}

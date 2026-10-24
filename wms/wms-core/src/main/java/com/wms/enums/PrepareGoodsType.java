package com.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author li-shuang
 */

@Getter
@AllArgsConstructor
public enum PrepareGoodsType {
    SELL_PARE_YARN("01", "卖备纱"),
    PRODUCE("02", "生产"),
    RESERVE_STOCK("03", "备库存");

    private final String code;
    private final String desc;
}

package com.wms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author li shuang
 */
@Data
public class OrderExcludeQtyVo {
    /**
     * 明细id
     */
    private Long id;
    /**
     * 订单id
     */
    private Long orderId;
    /**
     * 不纳入量
     */
    private BigDecimal excludeQty;
}

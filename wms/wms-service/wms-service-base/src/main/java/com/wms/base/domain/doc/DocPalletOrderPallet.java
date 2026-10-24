package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 组托单-托盘组（一单多托）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_pallet_order_pallet")
public class DocPalletOrderPallet extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 组托单ID（外键 doc_pallet_order.id） */
    @TableField("order_id")
    private Long orderId;

    /** 序号 */
    private Integer seq;

    /** 托盘号 */
    @TableField("pallet_no")
    private String palletNo;

    /** 筒数 */
    private Integer tubes;

    /** 标重（KG） */
    @TableField("std_weight")
    private BigDecimal stdWeight;

    /** 净重（KG） */
    @TableField("net_weight")
    private BigDecimal netWeight;

    /** 毛重（KG） */
    @TableField("gross_weight")
    private BigDecimal grossWeight;

    /** 筒重（KG） */
    @TableField("tube_weight")
    private BigDecimal tubeWeight;

    /** 实际筒重（KG） */
    @TableField("real_tube")
    private BigDecimal realTube;

    /** 标定筒重（KG） */
    @TableField("std_tube")
    private BigDecimal stdTube;

    @TableField("create_time")
    private Date createTime;
}

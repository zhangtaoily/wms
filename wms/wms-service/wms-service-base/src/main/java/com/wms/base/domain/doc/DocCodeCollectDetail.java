package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 码单收录明细（逐箱采集）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_code_collect_detail")
public class DocCodeCollectDetail extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 码单ID（外键 doc_code_collect.id） */
    @TableField("collect_id")
    private Long collectId;

    /** 序号 */
    private Integer seq;

    /** 箱条码 */
    @TableField("box_barcode")
    private String boxBarcode;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 库存批号 */
    @TableField("inv_batch_no")
    private String invBatchNo;

    /** 筒数 */
    private Integer tubes;

    /** 标重（KG） */
    @TableField("std_weight")
    private BigDecimal stdWeight;

    /** 毛重（KG） */
    @TableField("gross_weight")
    private BigDecimal grossWeight;

    /** 净重（KG） */
    @TableField("net_weight")
    private BigDecimal netWeight;

    /** 皮重（KG） */
    private BigDecimal tare;

    /** 标定回潮 */
    @TableField("std_back")
    private BigDecimal stdBack;

    /** 实际回潮 */
    @TableField("real_back")
    private BigDecimal realBack;

    @TableField("create_time")
    private Date createTime;
}

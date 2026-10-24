package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盘存单箱明细（实盘逐箱称重记录，孙表：外键 doc_stock_count_detail.id）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_stock_count_box")
public class DocStockCountBox extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 盘存明细ID（外键 doc_stock_count_detail.id） */
    @TableField("count_detail_id")
    private Long countDetailId;

    /** 箱码 */
    @TableField("box_code")
    private String boxCode;

    /** 实盘标重（KG） */
    @TableField("real_std")
    private BigDecimal realStd;

    /** 实盘净重（KG） */
    @TableField("real_net")
    private BigDecimal realNet;

    /** 实盘毛重（KG） */
    @TableField("real_gross")
    private BigDecimal realGross;

    /** 皮重（KG） */
    private BigDecimal tare;

    /** 单筒重量（KG） */
    @TableField("tube_weight")
    private BigDecimal tubeWeight;

    @TableField("create_time")
    private Date createTime;
}

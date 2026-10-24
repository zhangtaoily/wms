package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盘存单明细（盘前/实盘/盈亏三栏对照，按 仓位×批次 行）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_stock_count_detail")
public class DocStockCountDetail extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 盘存单ID（外键 doc_stock_count.id） */
    @TableField("count_id")
    private Long countId;

    /** 序号 */
    private Integer seq;

    /** 仓位 */
    @TableField("loc_code")
    private String locCode;

    /** 仓位名称 */
    @TableField("loc_name")
    private String locName;

    /** 库存批号 */
    @TableField("inv_batch_no")
    private String invBatchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 盘前筒数（账面） */
    @TableField("pre_tubes")
    private Integer preTubes;

    /** 实盘筒数 */
    @TableField("real_tubes")
    private Integer realTubes;

    /** 盈亏筒数（实盘-盘前） */
    @TableField("diff_tubes")
    private Integer diffTubes;

    /** 盘前标重（KG） */
    @TableField("pre_std")
    private BigDecimal preStd;

    /** 实盘标重（KG） */
    @TableField("real_std")
    private BigDecimal realStd;

    /** 盈亏标重（KG） */
    @TableField("diff_std")
    private BigDecimal diffStd;

    /** 盘前毛重（KG） */
    @TableField("pre_gross")
    private BigDecimal preGross;

    /** 实盘毛重（KG） */
    @TableField("real_gross")
    private BigDecimal realGross;

    /** 盈亏毛重（KG） */
    @TableField("diff_gross")
    private BigDecimal diffGross;

    /** 盘前净重（KG） */
    @TableField("pre_net")
    private BigDecimal preNet;

    /** 实盘净重（KG） */
    @TableField("real_net")
    private BigDecimal realNet;

    /** 盈亏净重（KG） */
    @TableField("diff_net")
    private BigDecimal diffNet;

    /** 标定回潮（%） */
    @TableField("std_mr")
    private BigDecimal stdMr;

    /** 实际回潮（%） */
    @TableField("real_mr")
    private BigDecimal realMr;

    /** 色号 */
    @TableField("color_code")
    private String colorCode;

    /** 色名 */
    @TableField("color_name")
    private String colorName;

    /** 物料编码 */
    @TableField("material_code")
    private String materialCode;

    /** 纱支 */
    private String yarn;

    /** 原料成分 */
    private String comp;

    @TableField("create_time")
    private Date createTime;
}

package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 入库单明细（逐箱入库，行级同步轴）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_inbound_detail")
public class DocInboundDetail extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 入库单ID（外键 doc_inbound.id） */
    @TableField("inbound_id")
    private Long inboundId;

    /** 序号 */
    private Integer seq;

    /** 箱码 */
    @TableField("box_code")
    private String boxCode;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 库位 */
    @TableField("loc_code")
    private String locCode;

    /** 色号 */
    @TableField("color_code")
    private String colorCode;

    /** 色名 */
    @TableField("color_name")
    private String colorName;

    /** 原料成分 */
    private String comp;

    /** 物料编码 */
    @TableField("material_code")
    private String materialCode;

    /** 物料名称 */
    @TableField("material_name")
    private String materialName;

    /** 纱别 */
    private String yarn;

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

    /** 行同步状态（1=已同步 2=未同步） */
    @TableField("sync_status")
    private Integer syncStatus;

    @TableField("create_time")
    private Date createTime;
}

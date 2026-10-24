package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * 出库单明细（条码/批次行级扣账依据）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_outbound_detail")
public class DocOutboundDetail extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 出库单ID（外键 doc_outbound.id） */
    @TableField("outbound_id")
    private Long outboundId;

    /** 序号 */
    private Integer seq;

    /** ERP单号（行级来源） */
    @TableField("erp_doc_no")
    private String erpDocNo;

    /** ERP单据日期 */
    @TableField("erp_date")
    private LocalDate erpDate;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 批次信息（物料名称冗余） */
    @TableField("batch_info")
    private String batchInfo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 箱码（条码作业时） */
    @TableField("box_code")
    private String boxCode;

    /** 箱数 */
    private Integer boxes;

    /** 筒数 */
    private Integer tubes;

    /** 件数 */
    private Integer pieces;

    /** 标重（KG） */
    @TableField("std_weight")
    private BigDecimal stdWeight;

    /** 毛重（KG） */
    @TableField("gross_weight")
    private BigDecimal grossWeight;

    /** 净重（KG） */
    @TableField("net_weight")
    private BigDecimal netWeight;

    /** 物料编码 */
    @TableField("material_code")
    private String materialCode;

    /** 物料名称 */
    @TableField("material_name")
    private String materialName;

    /** 纱支 */
    private String yarn;

    /** 原料成分 */
    private String comp;

    /** 色号 */
    @TableField("color_code")
    private String colorCode;

    /** PDA拣货核对状态（0=待核对 1=已核对） */
    @TableField("check_status")
    private Integer checkStatus;

    /** 核对操作员 */
    private String checker;

    /** 核对时间 */
    @TableField("check_time")
    private Date checkTime;

    @TableField("create_time")
    private Date createTime;
}

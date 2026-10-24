package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 出库通知单明细（行级送货安排）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_out_notice_detail")
public class DocOutNoticeDetail extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 出库通知ID（外键 doc_out_notice.id） */
    @TableField("notice_id")
    private Long noticeId;

    /** 序号 */
    private Integer seq;

    /** 送货方式（汽运/快递/自提） */
    @TableField("delive_type")
    private String deliveType;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 物料编码 */
    @TableField("material_code")
    private String materialCode;

    /** 物料名称 */
    @TableField("material_name")
    private String materialName;

    /** 物料规格（纱支） */
    private String spec;

    /** 原料明细（成分） */
    private String comp;

    /** 条染打版色号 */
    @TableField("color_top")
    private String colorTop;

    /** 绞染打版色号 */
    @TableField("color_piece")
    private String colorPiece;

    /** 公司色号 */
    @TableField("color_comp")
    private String colorComp;

    /** 客户色号 */
    @TableField("color_cust")
    private String colorCust;

    /** 色名 */
    @TableField("color_name")
    private String colorName;

    /** 数量 */
    private BigDecimal qty;

    /** 单位（KG） */
    private String unit;

    /** 仓库 */
    @TableField("warehouse_name")
    private String warehouseName;

    /** 仓位 */
    @TableField("loc_code")
    private String locCode;

    private String creator;

    @TableField("create_time")
    private Date createTime;
}

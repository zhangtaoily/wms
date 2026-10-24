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
 * 出库通知单（FR-05：仅接受发货通知单/领料申请单/采购退料单三源导入或手工新建，按 ERP 单号去重）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_out_notice")
public class DocOutNotice extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 出库通知单号（引擎取号列，属性名固定 code） */
    @TableField("notice_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 通知来源（发货通知单/领料申请单/采购退料单/手工新建） */
    @TableField("notice_type")
    private String noticeType;

    /** 单据日期 */
    @TableField("notice_date")
    private LocalDate noticeDate;

    /** ERP单号（发货通知/销售订单） */
    @TableField("erp_no")
    private String erpNo;

    /** ERP单据日期 */
    @TableField("erp_date")
    private LocalDate erpDate;

    /** 销售员 */
    private String sales;

    /** 销售部门 */
    @TableField("sales_dept")
    private String salesDept;

    /** 客户ID */
    @TableField("customer_id")
    private Long customerId;

    /** 客户名称（冗余） */
    @TableField("customer_name")
    private String customerName;

    /** 供应商名称（集团内供应主体） */
    @TableField("supplier_name")
    private String supplierName;

    /** 数量（KG） */
    private BigDecimal qty;

    private String creator;

    @TableField("create_time")
    private Date createTime;

    /** 审核人（引擎 PASS 反射写入，Long 用户ID） */
    private Long auditor;

    /** 审核时间 */
    private java.time.LocalDateTime auditTime;

    private String updater;

    @TableField("update_time")
    private Date updateTime;
}

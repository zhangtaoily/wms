package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 入库单（生效后批次台账增加，回填 ERP 入库单号，同步轴幂等重试）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_inbound")
public class DocInbound extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 入库单号（引擎取号列，属性名固定 code） */
    @TableField("inbound_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 入库日期 */
    @TableField("inbound_date")
    private LocalDate inboundDate;

    /** 同步状态（1=已同步 2=未同步，引擎 CREATE 自动置 2） */
    @TableField("sync_status")
    private Integer syncStatus;

    /** ERP 单据编号 */
    @TableField("erp_doc_no")
    private String erpDocNo;

    /** ERP 单据类型 */
    @TableField("erp_doc_type")
    private String erpDocType;

    /** ERP 入库单号（同步成功回填） */
    @TableField("erp_in_no")
    private String erpInNo;

    /** 来源类型（ERP/WMS） */
    @TableField("source_type")
    private String sourceType;

    /** 仓库ID */
    @TableField("warehouse_id")
    private Long warehouseId;

    /** 仓库名称 */
    @TableField("warehouse_name")
    private String warehouseName;

    /** 入库类型 */
    @TableField("inbound_type")
    private String inboundType;

    /** 作业方式 */
    @TableField("work_mode")
    private String workMode;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 重量（KG） */
    private BigDecimal weight;

    /** 箱数 */
    private Integer boxes;

    private String creator;

    @TableField("create_time")
    private Date createTime;

    private String updater;

    @TableField("update_time")
    private Date updateTime;

    /** 审核人（引擎 PASS 反射写入，Long 用户ID） */
    private Long auditor;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 红冲标志（0=正常 1=红冲负向单） */
    @TableField("reverse_flag")
    private Integer reverseFlag;

    /** 红冲引用原单号 */
    @TableField("source_inbound_no")
    private String sourceInboundNo;

    /** 红冲引用原单ID */
    @TableField("source_inbound_id")
    private Long sourceInboundId;
}

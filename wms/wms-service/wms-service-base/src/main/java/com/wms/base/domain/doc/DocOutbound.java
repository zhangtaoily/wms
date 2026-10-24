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
 * 出库单（生效后批次台账扣减并生成仓储/作业任务，回写 ERP 出库单号，同步轴幂等重试；支持红冲负向）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_outbound")
public class DocOutbound extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 出库单号（引擎取号列，属性名固定 code） */
    @TableField("outbound_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 出库日期 */
    @TableField("outbound_date")
    private LocalDate outboundDate;

    /** 同步状态（1=已同步 2=未同步，引擎 CREATE 自动置 2） */
    @TableField("sync_status")
    private Integer syncStatus;

    /** ERP订单号（引入来源，如 XSXO0000123） */
    @TableField("erp_order_no")
    private String erpOrderNo;

    /** 回写ERP的出库单号（同步成功回填） */
    @TableField("erp_out_no")
    private String erpOutNo;

    /** 客户ID */
    @TableField("customer_id")
    private Long customerId;

    /** 客户名称（冗余） */
    @TableField("customer_name")
    private String customerName;

    /** 销售员 */
    private String sales;

    /** 仓库ID */
    @TableField("warehouse_id")
    private Long warehouseId;

    /** 仓库名称（冗余） */
    @TableField("warehouse_name")
    private String warehouseName;

    /** 出库类型（销售/调拨/领料/直接拣货/复磅检/采购退料/样品出库） */
    @TableField("outbound_type")
    private String outboundType;

    /** 作业方式（条码作业/批次作业） */
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

    /** 经手人（拣货完成人） */
    private String handler;

    /** 经手时间（拣货完成时间） */
    @TableField("handle_time")
    private Date handleTime;

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
    @TableField("source_outbound_no")
    private String sourceOutboundNo;

    /** 红冲引用原单ID */
    @TableField("source_outbound_id")
    private Long sourceOutboundId;
}

package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 立库仓储任务（出库单生效自动生成，任务号 AT 开头）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("asrs_wh_task")
public class AsrsWhTask extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 仓储任务号（引擎取号列，属性名固定 code） */
    @TableField("task_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 任务日期 */
    @TableField("task_date")
    private LocalDate taskDate;

    /** 任务类型（条码入库/条码出库/空托出库） */
    @TableField("task_type")
    private String taskType;

    /** 执行状态（待执行/执行中/已完成） */
    @TableField("exec_status")
    private String execStatus;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 物料编码 */
    @TableField("material_code")
    private String materialCode;

    /** 任务批号 */
    @TableField("task_batch_no")
    private String taskBatchNo;

    /** 出库类型 */
    @TableField("outbound_type")
    private String outboundType;

    /** ERP单号 */
    @TableField("erp_doc_no")
    private String erpDocNo;

    /** ERP单据类型 */
    @TableField("erp_doc_type")
    private String erpDocType;

    /** 标箱 */
    @TableField("std_box")
    private Integer stdBox;

    /** 箱数 */
    private Integer boxes;

    private String creator;

    @TableField("create_time")
    private Date createTime;

    private String updater;

    @TableField("update_time")
    private Date updateTime;

    /** 审核人（引擎 PASS 反射写入） */
    private Long auditor;

    /** 审核时间 */
    private LocalDateTime auditTime;
}

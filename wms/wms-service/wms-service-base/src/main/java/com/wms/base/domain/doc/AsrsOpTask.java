package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 立库作业任务（WCS 设备调度，随仓储任务自动下发，失败可重试）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("asrs_op_task")
public class AsrsOpTask extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** WCS任务ID（设备调度系统任务号） */
    @TableField("wcs_task_id")
    private String wcsTaskId;

    /** 关联仓储任务ID（asrs_wh_task.id） */
    @TableField("wh_task_id")
    private Long whTaskId;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 托盘号 */
    @TableField("pallet_no")
    private String palletNo;

    /** 源仓位 */
    @TableField("from_loc")
    private String fromLoc;

    /** 目标仓位（出口自动/拆托位/仓位号） */
    @TableField("to_loc")
    private String toLoc;

    /** 任务类型（条码入库/条码出库/托盘挪库） */
    @TableField("task_type")
    private String taskType;

    /** 作业类型（入库/出库/移库） */
    @TableField("op_type")
    private String opType;

    /** 出入口（出口自动/月台1号回路/月台2号回路/5#自动线） */
    private String port;

    /** 任务下发时间 */
    @TableField("task_time")
    private Date taskTime;

    /** 立库完成时间 */
    @TableField("asrs_time")
    private Date asrsTime;

    /** 执行状态（执行中/成功/失败） */
    @TableField("exec_status")
    private String execStatus;

    /** 备注 */
    private String remark;

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

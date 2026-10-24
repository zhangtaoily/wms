package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Date;

/**
 * 立库作业日志（WCS 上报流水，只读：按单据日期检索、不可修改）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("asrs_op_log")
public class AsrsOpLog extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 分析状态（待分析/已分析） */
    private String analysis;

    /** WCS上报时间 */
    @TableField("wcs_time")
    private Date wcsTime;

    /** 处理完成时间 */
    @TableField("proc_time")
    private Date procTime;

    /** 作业类型（入库/出库/移库） */
    @TableField("op_type")
    private String opType;

    /** 作业结果（正常/异常） */
    private String result;

    /** 源仓位 */
    @TableField("from_loc")
    private String fromLoc;

    /** 源位置名称 */
    @TableField("from_name")
    private String fromName;

    /** 目标仓位 */
    @TableField("to_loc")
    private String toLoc;

    /** 目标位置名称 */
    @TableField("to_name")
    private String toName;

    /** 托盘号 */
    @TableField("pallet_no")
    private String palletNo;

    /** 单据日期 */
    @TableField("doc_date")
    private LocalDate docDate;

    @TableField("create_time")
    private Date createTime;
}

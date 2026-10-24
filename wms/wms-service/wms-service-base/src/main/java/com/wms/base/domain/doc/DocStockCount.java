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
 * 盘存单（FR-09：按仓库/仓位生成盘点范围，逐箱核对批号/缸号/标重）
 * Q2：生效后生成盘盈亏调整记录（adjust_state=待确认），仓库主管确认后调整台账并回写 ERP
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_stock_count")
public class DocStockCount extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 盘存单号（引擎取号列，属性名固定 code，CV开头） */
    @TableField("count_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 盘存日期 */
    @TableField("count_date")
    private LocalDate countDate;

    /** 同步状态（1=已同步 2=未同步；主管确认后触发 ERP 数量变更回写） */
    @TableField("sync_status")
    private Integer syncStatus;

    /** ERP调整单号（盘盈亏回写成功后回填） */
    @TableField("erp_no")
    private String erpNo;

    /** 作业方式（条码作业/批次作业） */
    @TableField("work_mode")
    private String workMode;

    /** 仓库ID */
    @TableField("warehouse_id")
    private Long warehouseId;

    /** 仓库名称（冗余） */
    @TableField("warehouse_name")
    private String warehouseName;

    /** 盘存仓位 */
    @TableField("loc_code")
    private String locCode;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 实盘件数 */
    @TableField("real_pieces")
    private Integer realPieces;

    /** 实盘重量（KG） */
    @TableField("real_weight")
    private BigDecimal realWeight;

    /** 盘盈亏调整确认状态（1=待确认 2=已确认 3=已驳回；无盈亏为空，Q2 主管确认动作） */
    @TableField("adjust_state")
    private Integer adjustState;

    /** 调整确认人（仓库主管） */
    @TableField("adjust_confirm_by")
    private String adjustConfirmBy;

    /** 调整确认时间 */
    @TableField("adjust_confirm_time")
    private LocalDateTime adjustConfirmTime;

    private String remark;

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
}

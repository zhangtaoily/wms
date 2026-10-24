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
 * 移库单（FR-08：同仓库内仓位变更，托盘/箱/批次总量不变；生效后仓位变更同步 ERP）
 * 四种移库类型：托盘挪库/编码移库/批次移库/空托移库（Q3：跨仓转移走出库单+入库单组合）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_stock_move")
public class DocStockMove extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 移库单号（引擎取号列，属性名固定 code，MD开头） */
    @TableField("move_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 移库日期 */
    @TableField("move_date")
    private LocalDate moveDate;

    /** 同步状态（1=已同步 2=未同步，引擎 CREATE 自动置 2） */
    @TableField("sync_status")
    private Integer syncStatus;

    /** ERP调拨单号（DB开头，仓位变更回写载体） */
    @TableField("erp_no")
    private String erpNo;

    /** 移库类型（托盘挪库/编码移库/批次移库/空托移库） */
    @TableField("move_type")
    private String moveType;

    /** 批号（批次移库时） */
    @TableField("batch_no")
    private String batchNo;

    /** 源仓库ID */
    @TableField("from_warehouse_id")
    private Long fromWarehouseId;

    /** 源仓位 */
    @TableField("from_loc_code")
    private String fromLocCode;

    /** 目标仓库ID（Q3：须与源仓库相同） */
    @TableField("to_warehouse_id")
    private Long toWarehouseId;

    /** 目标仓位 */
    @TableField("to_loc_code")
    private String toLocCode;

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

    /** 红冲标志（0=正常 1=红冲反向单，Q4：红冲=源/目标仓位对调） */
    @TableField("reverse_flag")
    private Integer reverseFlag;

    /** 红冲引用原单号 */
    @TableField("source_move_no")
    private String sourceMoveNo;

    /** 红冲引用原单ID */
    @TableField("source_move_id")
    private Long sourceMoveId;
}

package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 移库单明细（按托盘/箱/批次层级记录仓位转移，数量不变）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_stock_move_detail")
public class DocStockMoveDetail extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 移库单ID（外键 doc_stock_move.id） */
    @TableField("move_id")
    private Long moveId;

    /** 序号 */
    private Integer seq;

    /** 箱码（编码移库/托盘挪库时） */
    @TableField("box_code")
    private String boxCode;

    /** 托盘号（托盘挪库/空托移库时） */
    @TableField("pallet_no")
    private String palletNo;

    /** 批号（批次移库时） */
    @TableField("batch_no")
    private String batchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 目标仓库（冗余） */
    @TableField("to_warehouse_name")
    private String toWarehouseName;

    /** 目标仓位 */
    @TableField("to_loc_code")
    private String toLocCode;

    @TableField("create_time")
    private Date createTime;
}

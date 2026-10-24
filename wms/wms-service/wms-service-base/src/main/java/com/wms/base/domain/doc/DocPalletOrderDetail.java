package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 组托单-箱明细（同托箱码归属一致：同 palletNo 的箱必须同批号）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_pallet_order_detail")
public class DocPalletOrderDetail extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 组托单ID（外键 doc_pallet_order.id，引擎按 foreignKey 自动填充） */
    @TableField("order_id")
    private Long orderId;

    /** 托盘组ID（可选，行数据携带） */
    @TableField("pallet_id")
    private Long palletId;

    /** 托盘号（平挂结构下箱明细与托盘组的关联键，前端从选中托盘带出） */
    @TableField("pallet_no")
    private String palletNo;

    /** 序号 */
    private Integer seq;

    /** 箱码 */
    @TableField("box_code")
    private String boxCode;

    /** 标出 */
    @TableField("mark_out")
    private String markOut;

    /** 箱状态 */
    @TableField("box_status")
    private String boxStatus;

    /** 仓库 */
    @TableField("warehouse_name")
    private String warehouseName;

    /** 库位编号 */
    @TableField("loc_code")
    private String locCode;

    /** 库位名称 */
    @TableField("loc_name")
    private String locName;

    /** 箱子数 */
    @TableField("box_count")
    private Integer boxCount;

    /** 标重（KG） */
    @TableField("std_weight")
    private BigDecimal stdWeight;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    @TableField("create_time")
    private Date createTime;
}

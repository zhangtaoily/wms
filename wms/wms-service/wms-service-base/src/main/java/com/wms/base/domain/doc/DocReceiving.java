package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Date;

/**
 * 收货清单（ERP 下发，在途流转，不产库存）
 * 无 code/pactState 属性：引擎自动跳过取号与状态机处理
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_receiving")
public class DocReceiving extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 收货单号 */
    @TableField("receive_no")
    private String receiveNo;

    /** 来源类型（ERP） */
    @TableField("source_type")
    private String sourceType;

    /** 发货日期 */
    @TableField("out_date")
    private LocalDate outDate;

    /** 供应商ID */
    @TableField("supplier_id")
    private Long supplierId;

    /** 供应商编码 */
    @TableField("supplier_code")
    private String supplierCode;

    /** 供应商名称 */
    @TableField("supplier_name")
    private String supplierName;

    /** ERP 出库单号 */
    @TableField("out_no")
    private String outNo;

    /** 在途状态（在途/到达/收讫） */
    @TableField("tran_status")
    private String tranStatus;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}

package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 组托单（三种组托方式，生成托盘台账）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_pallet_order")
public class DocPalletOrder extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 组托单号（引擎取号列，属性名固定 code） */
    @TableField("pallet_order_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 组托类型（手工组托/扫码组托/按明细大小添加） */
    @TableField("pallet_type")
    private String palletType;

    /** 组托日期 */
    @TableField("order_date")
    private LocalDateTime orderDate;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 缸号 */
    @TableField("vat_no")
    private String vatNo;

    /** 备注 */
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

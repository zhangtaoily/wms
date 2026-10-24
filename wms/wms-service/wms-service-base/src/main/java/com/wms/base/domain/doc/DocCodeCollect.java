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
 * 码单收录单（箱码信息采集，累计件量生成箱码台账）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_code_collect")
public class DocCodeCollect extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 码单编号（引擎取号列，属性名固定 code） */
    @TableField("code_no")
    private String code;

    /** 单据状态（引擎状态机，属性名固定 pactState） */
    @TableField("status")
    private Integer pactState;

    /** 业务类型（生产码单/采购码单） */
    @TableField("biz_type")
    private String bizType;

    /** 收录日期 */
    @TableField("collect_date")
    private LocalDate collectDate;

    /** ERP 单号 */
    @TableField("erp_no")
    private String erpNo;

    /** 批号 */
    @TableField("batch_no")
    private String batchNo;

    /** 件量合计 */
    @TableField("qty")
    private BigDecimal qty;

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

package com.wms.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wms.common.core.domain.BaseEntity;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * (WfConfig)实体类
 *
 * @author makejava
 * @since 2024-08-13 13:12:28
 */
@Data
@TableName("wf_config")
public class WfConfig extends BaseEntity {
    private static final long serialVersionUID = -72402548453567584L;
    /**
     * 单据编号
     */
    private String code;
    /**
     * 单据日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate pactDate;
    /**
     * 单据状态
     */
    private Integer pactState;
    /**
     * 主表SQL
     */
    private String sqlMain;
    /**
     * 子表名称
     */
    private String childDbName;
    /**
     * 子表SQL
     */
    private String sqlChild;
    /**
     * 单据类型
     */
    private String functionName;
    /**
     * 工作流ID
     */
    private String wfId;
    /**
     * 审核人
     */
    private String auditor;
    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    /**
     * 是否启用
     */
    private Integer enabled;
}

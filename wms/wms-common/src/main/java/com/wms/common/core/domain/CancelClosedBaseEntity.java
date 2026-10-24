package com.wms.common.core.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 关闭和取消实体类
 * @author li-shuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CancelClosedBaseEntity extends BaseEntity {
    /**
     * 是否关闭
     */
    private Integer isClosed;
    /**
     * 关闭人
     */
    private String closedBy;
    /**
     * 关闭时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedTime;
    /**
     * 是否取消
     */
    private Integer isCancel;
    /**
     * 取消人
     */
    private String cancelBy;
    /**
     * 取消审批状态
     */
    private Integer cancelOaState;
    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    /**
     * 审核人
     */
    private String auditor;
    /**
     * 审核日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    /**
     *  取消说明
     */
    private String cancelReason;
}

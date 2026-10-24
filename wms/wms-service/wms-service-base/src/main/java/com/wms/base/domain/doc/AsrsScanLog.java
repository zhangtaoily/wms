package com.wms.base.domain.doc;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 扫码终端作业日志（立库扫码终端 ASRS SCAN TERMINAL 实时流水，M6 FR-07）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("asrs_scan_log")
public class AsrsScanLog extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 扫码时间 */
    @TableField("scan_time")
    private Date scanTime;

    /** 动作（入库请求/入库完成/出库请求/出库完成/拣选确认） */
    private String action;

    /** 仓位 */
    @TableField("loc_code")
    private String locCode;

    /** 托盘号 */
    @TableField("pallet_no")
    private String palletNo;

    /** 结果（成功/失败） */
    private String result;

    /** 消息（失败原因等） */
    private String msg;

    @TableField("create_time")
    private Date createTime;
}

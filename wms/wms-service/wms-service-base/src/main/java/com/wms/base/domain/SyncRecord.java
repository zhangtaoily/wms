package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 外部系统同步记录（M7 同步工程化）
 * 幂等键 (biz_type, doc_no, direction)：同一单据重复触发同步不产生重复 ERP 单号（验收①）；
 * 未同步记录由定时补偿任务按指数退避重试（Q1：次数/间隔可配置），超限转人工重推（同步监控页）。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sync_record")
public class SyncRecord extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 目标系统（ERP/MES） */
    @TableField("target_system")
    private String targetSystem;

    /** 同步方向（引入=ERP/MES→WMS / 回写=WMS→ERP） */
    private String direction;

    /** 单据类型（入库单/出库单/移库单/盘存单） */
    @TableField("biz_type")
    private String bizType;

    /** 本系统单据ID */
    @TableField("doc_id")
    private Long docId;

    /** 本系统单据号 */
    @TableField("doc_no")
    private String docNo;

    /** 外部系统单号（ERP/MES单号，成功后回填） */
    @TableField("ext_no")
    private String extNo;

    /** 同步状态（成功/未同步/失败；失败=超限转人工） */
    private String status;

    /** 重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 最近同步消息（成功确认号/失败原因） */
    @TableField("last_msg")
    private String lastMsg;

    /** 最近同步时间 */
    @TableField("last_time")
    private Date lastTime;

    /** 创建人（触发同步的操作员或系统） */
    private String creator;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}

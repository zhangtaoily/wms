package com.wms.sync.job;

import com.wms.base.domain.SyncRecord;
import com.wms.base.service.doc.ErpInboundSyncStub;
import com.wms.base.service.doc.ErpOutboundSyncStub;
import com.wms.base.service.doc.ErpStockCountSyncStub;
import com.wms.base.service.doc.ErpStockMoveSyncStub;
import com.wms.base.service.doc.SyncRecordService;
import com.wms.common.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 同步补偿定时任务（M7，PRD 闭环⑤ + Q1）
 * 每 30s 扫描 sync_record 中"未同步"记录，到期（指数退避 base_interval × 2^retry_count，可配置）则重新触发对应 ERP 桩；
 * 重试超限（maxRetry，可配置）的记录由 SyncRecordService 置为"失败"转人工，同步监控页人工重推。
 * 桩内以 ERP 单号幂等判重，重复触发不产生重复 ERP 单号（验收①）。
 * 说明：Spring 默认单线程调度器，Job 串行执行无并发问题。
 */
@Slf4j
@Component
public class SyncRetryJob {

    /** 单轮最多处理条数（防止单轮过长，剩余下一轮继续） */
    private static final int BATCH_LIMIT = 20;

    @Resource
    private SyncRecordService syncRecordService;
    @Resource
    private ErpInboundSyncStub erpInboundSyncStub;
    @Resource
    private ErpOutboundSyncStub erpOutboundSyncStub;
    @Resource
    private ErpStockMoveSyncStub erpStockMoveSyncStub;
    @Resource
    private ErpStockCountSyncStub erpStockCountSyncStub;

    /** 定时补偿：fixedDelay 上一轮结束后 30s 再扫（间隔节奏由退避配置控制，不在 cron 层） */
    @Scheduled(fixedDelay = 30_000L, initialDelay = 60_000L)
    public void retryPendingSync() {
        List<SyncRecord> dueList;
        try {
            dueList = syncRecordService.dueList(BATCH_LIMIT);
        } catch (Exception ex) {
            log.error("同步补偿任务读取到期记录失败", ex);
            return;
        }
        if (dueList.isEmpty()) {
            return;
        }
        log.info("同步补偿任务：本轮到期待重试 {} 条", dueList.size());
        for (SyncRecord rec : dueList) {
            if (rec.getDocId() == null) {
                continue;
            }
            try {
                Tuple2<Boolean, String> result = dispatch(rec);
                log.info("同步补偿：{} {} 重试{}（{}）", rec.getBizType(), rec.getDocNo(),
                    result.getFrist() ? "成功" : "未成功", result.getSecond());
            } catch (Exception ex) {
                log.error("同步补偿：{} {} 重试异常", rec.getBizType(), rec.getDocNo(), ex);
            }
        }
    }

    /** 按单据类型路由对应 ERP 桩（桩内部写 sync_record，超限自动转人工） */
    private Tuple2<Boolean, String> dispatch(SyncRecord rec) {
        switch (rec.getBizType() == null ? "" : rec.getBizType()) {
            case SyncRecordService.BIZ_INBOUND:
                return erpInboundSyncStub.sync(rec.getDocId());
            case SyncRecordService.BIZ_OUTBOUND:
                return erpOutboundSyncStub.sync(rec.getDocId());
            case SyncRecordService.BIZ_STOCK_MOVE:
                return erpStockMoveSyncStub.sync(rec.getDocId());
            case SyncRecordService.BIZ_STOCK_COUNT:
                return erpStockCountSyncStub.sync(rec.getDocId());
            default:
                log.warn("同步补偿跳过未知单据类型：{}", rec.getBizType());
                return Tuple2.of(false, "未知单据类型");
        }
    }
}

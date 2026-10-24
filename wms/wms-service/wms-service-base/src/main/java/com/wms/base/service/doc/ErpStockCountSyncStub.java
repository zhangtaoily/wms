package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.wms.base.domain.doc.DocStockCount;
import com.wms.base.mapper.doc.DocStockCountMapper;
import com.wms.common.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * ERP 盘存同步桩（M5 同步轴，Q2 盘盈亏数量变更回写）
 * 规则：
 * 1. 幂等判重：以 ERP 调整单号 erp_no 判重，已同步直接返回成功
 * 2. 前置校验：盘盈亏调整未经仓库主管确认（adjust_state≠已确认）不得回写 ERP
 * 3. 成功回填：erp_no = "TZ" + 盘存单号流水，sync_status=1
 */
@Slf4j
@Service
public class ErpStockCountSyncStub {

    /** 盘盈亏调整确认状态：已确认 */
    private static final int ADJUST_CONFIRMED = 2;

    @Resource
    private DocStockCountMapper docStockCountMapper;
    @Resource
    private SyncRecordService syncRecordService;

    /**
     * 同步盘存单数量变更到 ERP（模拟接口）
     *
     * @param countId 盘存单ID
     * @return Tuple2(成功?, ERP调整单号或失败原因)
     */
    public Tuple2<Boolean, String> sync(Long countId) {
        DocStockCount doc = docStockCountMapper.selectById(countId);
        if (doc == null) {
            return Tuple2.of(false, "盘存单不存在");
        }
        // 幂等：以 ERP 调整单号判重
        if (StrUtil.isNotEmpty(doc.getErpNo())) {
            syncRecordService.record(SyncRecordService.BIZ_STOCK_COUNT, countId, doc.getCode(),
                doc.getErpNo(), true, "同步成功：" + doc.getErpNo());
            return Tuple2.of(true, doc.getErpNo());
        }
        // 主管确认前不得回写（Q2：确认后回写 ERP 数量变更）
        if (doc.getAdjustState() == null || doc.getAdjustState() != ADJUST_CONFIRMED) {
            log.warn("盘存单{}同步ERP失败：盘盈亏调整未经主管确认", doc.getCode());
            syncRecordService.record(SyncRecordService.BIZ_STOCK_COUNT, countId, doc.getCode(),
                null, false, "盘盈亏调整未经主管确认，无法同步");
            return Tuple2.of(false, "盘盈亏调整未经主管确认，无法同步");
        }
        // 模拟 ERP 接口成功：回填 ERP 调整单号 + 同步状态
        String erpNo = "TZ" + doc.getCode().replaceFirst("^CV", "");
        DocStockCount upd = new DocStockCount();
        upd.setId(countId);
        upd.setErpNo(erpNo);
        upd.setSyncStatus(1);
        upd.setUpdateTime(new Date());
        docStockCountMapper.updateById(upd);
        log.info("盘存单{}同步ERP数量变更成功：{}", doc.getCode(), erpNo);
        syncRecordService.record(SyncRecordService.BIZ_STOCK_COUNT, countId, doc.getCode(),
            erpNo, true, "同步成功：" + erpNo);
        return Tuple2.of(true, erpNo);
    }
}

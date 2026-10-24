package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.wms.base.domain.doc.DocStockMove;
import com.wms.base.mapper.doc.DocStockMoveMapper;
import com.wms.common.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * ERP 移库同步桩（M5 同步轴，FR-08 仓位变更回写；M7 接入 sync_record 幂等落账）
 * 规则：
 * 1. 幂等判重：以 ERP 调拨单号 erp_no 判重，已同步直接返回成功
 * 2. 成功回填：erp_no = "DB" + 移库单号流水（ERP 启用仓位（货位）管理，仓位变更以调拨单号承载）
 */
@Slf4j
@Service
public class ErpStockMoveSyncStub {

    @Resource
    private DocStockMoveMapper docStockMoveMapper;
    @Resource
    private SyncRecordService syncRecordService;

    /**
     * 同步移库单仓位变更到 ERP（模拟接口）
     *
     * @param moveId 移库单ID
     * @return Tuple2(成功?, ERP调拨单号或失败原因)
     */
    public Tuple2<Boolean, String> sync(Long moveId) {
        DocStockMove doc = docStockMoveMapper.selectById(moveId);
        if (doc == null) {
            return Tuple2.of(false, "移库单不存在");
        }
        // 幂等：以 ERP 调拨单号判重
        if (StrUtil.isNotEmpty(doc.getErpNo())) {
            syncRecordService.record(SyncRecordService.BIZ_STOCK_MOVE, moveId, doc.getCode(),
                doc.getErpNo(), true, "同步成功：" + doc.getErpNo());
            return Tuple2.of(true, doc.getErpNo());
        }
        // 模拟 ERP 接口成功：回填 ERP 调拨单号 + 同步状态
        String erpNo = "DB" + doc.getCode().replaceFirst("^MD", "");
        DocStockMove upd = new DocStockMove();
        upd.setId(moveId);
        upd.setErpNo(erpNo);
        upd.setSyncStatus(1);
        upd.setUpdateTime(new Date());
        docStockMoveMapper.updateById(upd);
        log.info("移库单{}同步ERP仓位变更成功：{}", doc.getCode(), erpNo);
        syncRecordService.record(SyncRecordService.BIZ_STOCK_MOVE, moveId, doc.getCode(),
            erpNo, true, "同步成功：" + erpNo);
        return Tuple2.of(true, erpNo);
    }
}

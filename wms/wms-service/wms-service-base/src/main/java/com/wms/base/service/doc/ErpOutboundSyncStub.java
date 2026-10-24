package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.wms.base.domain.doc.DocOutbound;
import com.wms.base.mapper.doc.DocOutboundMapper;
import com.wms.common.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * ERP 出库同步桩（M4 同步轴；M7 接入 sync_record 幂等落账）
 * 规则：
 * 1. 幂等判重：以 ERP 出库单号 erp_out_no 判重，已同步直接返回成功
 * 2. 失败停留：erp_order_no 为空视为 ERP 侧校验失败，停留未同步（sync_status=2），可重复触发
 * 3. 成功回填：erp_out_no = "ERSO" + erp_order_no，sync_status=1
 */
@Slf4j
@Service
public class ErpOutboundSyncStub {

    @Resource
    private DocOutboundMapper docOutboundMapper;
    @Resource
    private SyncRecordService syncRecordService;

    /**
     * 同步出库单到 ERP（模拟接口）
     *
     * @param outboundId 出库单ID
     * @return Tuple2(成功?, ERP出库单号或失败原因)
     */
    public Tuple2<Boolean, String> sync(Long outboundId) {
        DocOutbound doc = docOutboundMapper.selectById(outboundId);
        if (doc == null) {
            return Tuple2.of(false, "出库单不存在");
        }
        // 幂等：以 ERP 出库单号判重
        if (StrUtil.isNotEmpty(doc.getErpOutNo())) {
            syncRecordService.record(SyncRecordService.BIZ_OUTBOUND, outboundId, doc.getCode(),
                doc.getErpOutNo(), true, "同步成功：" + doc.getErpOutNo());
            return Tuple2.of(true, doc.getErpOutNo());
        }
        // ERP 侧校验：缺 ERP 订单号，同步失败，停留未同步
        if (StrUtil.isEmpty(doc.getErpOrderNo())) {
            log.warn("出库单{}同步ERP失败：ERP订单号为空", doc.getCode());
            syncRecordService.record(SyncRecordService.BIZ_OUTBOUND, outboundId, doc.getCode(),
                null, false, "ERP订单号为空，无法同步");
            return Tuple2.of(false, "ERP订单号为空，无法同步");
        }
        // 模拟 ERP 接口成功：回填 ERP 出库单号 + 同步状态
        String erpOutNo = "ERSO" + doc.getErpOrderNo();
        DocOutbound upd = new DocOutbound();
        upd.setId(outboundId);
        upd.setErpOutNo(erpOutNo);
        upd.setSyncStatus(1);
        upd.setUpdateTime(new Date());
        docOutboundMapper.updateById(upd);
        log.info("出库单{}同步ERP成功：{}", doc.getCode(), erpOutNo);
        syncRecordService.record(SyncRecordService.BIZ_OUTBOUND, outboundId, doc.getCode(),
            erpOutNo, true, "同步成功：" + erpOutNo);
        return Tuple2.of(true, erpOutNo);
    }
}

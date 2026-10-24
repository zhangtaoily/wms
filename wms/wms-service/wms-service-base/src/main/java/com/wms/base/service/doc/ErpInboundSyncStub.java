package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.wms.base.domain.doc.DocInbound;
import com.wms.base.mapper.doc.DocInboundMapper;
import com.wms.common.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * ERP 入库同步桩（M3 同步轴；M7 接入 sync_record 幂等落账）
 * 规则：
 * 1. 幂等判重：以 ERP 入库单号 erp_in_no 判重，已同步直接返回成功
 * 2. 失败注入：erp_doc_no 为空视为 ERP 侧校验失败，停留未同步（sync_status=2），可重复触发
 * 3. 成功回填：erp_in_no = "ERPIN" + erp_doc_no，sync_status=1
 */
@Slf4j
@Service
public class ErpInboundSyncStub {

    @Resource
    private DocInboundMapper docInboundMapper;
    @Resource
    private SyncRecordService syncRecordService;

    /**
     * 同步入库单到 ERP（模拟接口）
     *
     * @param inboundId 入库单ID
     * @return Tuple2(成功?, ERP入库单号或失败原因)
     */
    public Tuple2<Boolean, String> sync(Long inboundId) {
        DocInbound doc = docInboundMapper.selectById(inboundId);
        if (doc == null) {
            return Tuple2.of(false, "入库单不存在");
        }
        // 幂等：以 ERP 入库单号判重
        if (StrUtil.isNotEmpty(doc.getErpInNo())) {
            syncRecordService.record(SyncRecordService.BIZ_INBOUND, inboundId, doc.getCode(),
                doc.getErpInNo(), true, "同步成功：" + doc.getErpInNo());
            return Tuple2.of(true, doc.getErpInNo());
        }
        // ERP 侧校验：缺 ERP 单据编号，同步失败，停留未同步
        if (StrUtil.isEmpty(doc.getErpDocNo())) {
            log.warn("入库单{}同步ERP失败：ERP单据编号为空", doc.getCode());
            syncRecordService.record(SyncRecordService.BIZ_INBOUND, inboundId, doc.getCode(),
                null, false, "ERP单据编号为空，无法同步");
            return Tuple2.of(false, "ERP单据编号为空，无法同步");
        }
        // 模拟 ERP 接口成功：回填 ERP 入库单号 + 同步状态
        String erpInNo = "ERPIN" + doc.getErpDocNo();
        DocInbound upd = new DocInbound();
        upd.setId(inboundId);
        upd.setErpInNo(erpInNo);
        upd.setSyncStatus(1);
        upd.setUpdateTime(new Date());
        docInboundMapper.updateById(upd);
        log.info("入库单{}同步ERP成功：{}", doc.getCode(), erpInNo);
        syncRecordService.record(SyncRecordService.BIZ_INBOUND, inboundId, doc.getCode(),
            erpInNo, true, "同步成功：" + erpInNo);
        return Tuple2.of(true, erpInNo);
    }
}

package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.base.domain.InvBox;
import com.wms.base.domain.InvPallet;
import com.wms.base.domain.doc.DocInbound;
import com.wms.base.domain.doc.DocOutbound;
import com.wms.base.domain.doc.DocStockCount;
import com.wms.base.domain.doc.DocStockMove;
import com.wms.base.mapper.InvBoxMapper;
import com.wms.base.mapper.InvPalletMapper;
import com.wms.base.mapper.doc.DocInboundMapper;
import com.wms.base.mapper.doc.DocOutboundMapper;
import com.wms.base.mapper.doc.DocStockCountMapper;
import com.wms.base.mapper.doc.DocStockMoveMapper;
import com.wms.common.exception.ServiceException;
import com.wms.service.common.service.BillStatusEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PDA 工作台聚合服务（M6）：PDA 与 PC 同一单据体系（同表同服务），
 * 本服务仅提供移动端视图所需的精简聚合查询：
 * 1. 扫托查询：托盘→箱码→批次→仓位 逐级追溯（验收⑤）
 * 2. 入库/出库待办：已生效单据精简列表（明细走通用 getDetails 引擎接口）
 * 3. 同步状态查询：四类单据 sync_status 聚合（闭环⑤），未同步单据可幂等重试（reSend）
 * 码单收录/组托/移库的写操作由 PDA 直接走通用 save/submit 引擎接口，与 PC 同源（验收④）
 */
@Service
public class PdaService {

    @Resource
    private InvPalletMapper invPalletMapper;
    @Resource
    private InvBoxMapper invBoxMapper;
    @Resource
    private DocInboundMapper docInboundMapper;
    @Resource
    private DocOutboundMapper docOutboundMapper;
    @Resource
    private DocStockMoveMapper docStockMoveMapper;
    @Resource
    private DocStockCountMapper docStockCountMapper;

    /**
     * 扫托查询：托盘台账 + 托下箱码明细（批号/缸号/仓位/状态）
     */
    public Map<String, Object> palletTrace(String palletNo) {
        if (StrUtil.isEmpty(palletNo)) {
            throw new ServiceException("请扫描或输入托盘号");
        }
        QueryWrapper<InvPallet> palletQw = new QueryWrapper<>();
        palletQw.eq("pallet_no", palletNo);
        InvPallet pallet = invPalletMapper.selectOne(palletQw);
        if (pallet == null) {
            throw new ServiceException("托盘" + palletNo + "不存在");
        }
        QueryWrapper<InvBox> boxQw = new QueryWrapper<>();
        boxQw.eq("pallet_no", palletNo).orderByAsc("id");
        List<InvBox> boxes = invBoxMapper.selectList(boxQw);
        List<Map<String, Object>> boxRows = new ArrayList<>();
        int inStock = 0;
        for (InvBox box : boxes) {
            if ("在库".equals(box.getStatus())) {
                inStock++;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("boxCode", box.getBoxCode());
            row.put("batchNo", box.getBatchNo());
            row.put("vatNo", box.getVatNo());
            row.put("locCode", box.getLocCode());
            row.put("warehouseName", box.getWarehouseName());
            row.put("stdWeight", box.getStdWeight());
            row.put("tubes", box.getTubes());
            row.put("status", box.getStatus());
            boxRows.add(row);
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> palletCard = new LinkedHashMap<>();
        palletCard.put("palletNo", pallet.getPalletNo());
        // 托盘自身仓位可能为空（组托不落仓位、入库只写箱仓位）：回退取托下首行箱码仓位
        String palletLoc = pallet.getLocCode();
        if (StrUtil.isEmpty(palletLoc)) {
            for (InvBox box : boxes) {
                if ("在库".equals(box.getStatus()) && StrUtil.isNotEmpty(box.getLocCode())) {
                    palletLoc = box.getLocCode();
                    break;
                }
            }
        }
        palletCard.put("locCode", palletLoc);
        palletCard.put("status", pallet.getStatus());
        palletCard.put("isEmpty", pallet.getIsEmpty());
        palletCard.put("batchNo", pallet.getBatchNo());
        palletCard.put("boxCount", pallet.getBoxCount());
        palletCard.put("inStockBoxes", inStock);
        result.put("pallet", palletCard);
        result.put("boxes", boxRows);
        return result;
    }

    /** PDA 入库待办：已生效入库单精简列表（建议仓位=明细/台账仓位，明细走通用接口） */
    public List<Map<String, Object>> pendingInbounds() {
        QueryWrapper<DocInbound> qw = new QueryWrapper<>();
        qw.eq("status", BillStatusEnum.PASS.getCode()).orderByDesc("id").last("limit 20");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (DocInbound doc : docInboundMapper.selectList(qw)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", doc.getId());
            row.put("code", doc.getCode());
            row.put("warehouseName", doc.getWarehouseName());
            row.put("inboundType", doc.getInboundType());
            row.put("batchNo", doc.getBatchNo());
            row.put("vatNo", doc.getVatNo());
            row.put("boxes", doc.getBoxes());
            row.put("weight", doc.getWeight());
            row.put("syncStatus", doc.getSyncStatus());
            rows.add(row);
        }
        return rows;
    }

    /** PDA 出库待办：已生效出库单精简列表 */
    public List<Map<String, Object>> pendingOutbounds() {
        QueryWrapper<DocOutbound> qw = new QueryWrapper<>();
        qw.eq("status", BillStatusEnum.PASS.getCode()).orderByDesc("id").last("limit 20");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (DocOutbound doc : docOutboundMapper.selectList(qw)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", doc.getId());
            row.put("code", doc.getCode());
            row.put("warehouseName", doc.getWarehouseName());
            row.put("outboundType", doc.getOutboundType());
            row.put("customerName", doc.getCustomerName());
            row.put("batchNo", doc.getBatchNo());
            row.put("vatNo", doc.getVatNo());
            row.put("boxes", doc.getBoxes());
            row.put("weight", doc.getWeight());
            row.put("syncStatus", doc.getSyncStatus());
            rows.add(row);
        }
        return rows;
    }

    /** 同步状态查询：入库/出库/移库/盘存 四类单据同步状态聚合（未同步可幂等重试） */
    public List<Map<String, Object>> syncStatusList() {
        List<Map<String, Object>> rows = new ArrayList<>();
        QueryWrapper<DocInbound> inQw = new QueryWrapper<>();
        inQw.eq("status", BillStatusEnum.PASS.getCode()).orderByDesc("update_time").last("limit 20");
        for (DocInbound doc : docInboundMapper.selectList(inQw)) {
            rows.add(syncRow("WMS_INBOUND", "入库单", doc.getId(), doc.getCode(), doc.getSyncStatus(), doc.getErpInNo(), doc.getUpdateTime()));
        }
        QueryWrapper<DocOutbound> outQw = new QueryWrapper<>();
        outQw.eq("status", BillStatusEnum.PASS.getCode()).orderByDesc("update_time").last("limit 20");
        for (DocOutbound doc : docOutboundMapper.selectList(outQw)) {
            rows.add(syncRow("WMS_OUTBOUND", "出库单", doc.getId(), doc.getCode(), doc.getSyncStatus(), doc.getErpOutNo(), doc.getUpdateTime()));
        }
        QueryWrapper<DocStockMove> moveQw = new QueryWrapper<>();
        moveQw.eq("status", BillStatusEnum.PASS.getCode()).orderByDesc("update_time").last("limit 20");
        for (DocStockMove doc : docStockMoveMapper.selectList(moveQw)) {
            rows.add(syncRow("WMS_STOCK_MOVE", "移库单", doc.getId(), doc.getCode(), doc.getSyncStatus(), doc.getErpNo(), doc.getUpdateTime()));
        }
        QueryWrapper<DocStockCount> countQw = new QueryWrapper<>();
        countQw.eq("status", BillStatusEnum.PASS.getCode()).orderByDesc("update_time").last("limit 20");
        for (DocStockCount doc : docStockCountMapper.selectList(countQw)) {
            rows.add(syncRow("WMS_STOCK_COUNT", "盘存单", doc.getId(), doc.getCode(), doc.getSyncStatus(), doc.getErpNo(), doc.getUpdateTime()));
        }
        rows.sort((a, b) -> String.valueOf(b.get("updateTime")).compareTo(String.valueOf(a.get("updateTime"))));
        return rows;
    }

    private Map<String, Object> syncRow(String functionName, String docType, Long id, String code,
                                        Integer syncStatus, String erpNo, Object updateTime) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("functionName", functionName);
        row.put("docType", docType);
        row.put("id", id);
        row.put("code", code);
        row.put("syncStatus", syncStatus);
        row.put("erpNo", erpNo);
        row.put("updateTime", updateTime);
        return row;
    }

    /** 箱码查询（码单收录回填：扫描箱码带出批号/缸号/标重） */
    public Map<String, Object> boxTrace(String boxCode) {
        if (StrUtil.isEmpty(boxCode)) {
            throw new ServiceException("请扫描箱码");
        }
        QueryWrapper<InvBox> qw = new QueryWrapper<>();
        qw.eq("box_code", boxCode);
        InvBox box = invBoxMapper.selectOne(qw);
        Map<String, Object> result = new HashMap<>();
        if (box == null) {
            result.put("exists", false);
            return result;
        }
        result.put("exists", true);
        result.put("boxCode", box.getBoxCode());
        result.put("batchNo", box.getBatchNo());
        result.put("vatNo", box.getVatNo());
        result.put("status", box.getStatus());
        result.put("locCode", box.getLocCode());
        result.put("palletNo", box.getPalletNo());
        result.put("tubes", box.getTubes());
        result.put("stdWeight", box.getStdWeight());
        result.put("grossWeight", box.getGrossWeight());
        result.put("netWeight", box.getNetWeight());
        return result;
    }
}

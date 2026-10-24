package com.wms.sync.service;

import cn.hutool.core.util.StrUtil;
import com.wms.base.mapper.SyncRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 对账服务（M7，PRD 7.4 闭环规则）：
 * 1. 同步完整性对账：生效单据的同步记录中非"成功"清单（含重试次数/失败原因），不平项按单据定位重同步
 * 2. 台账对账：WMS 批次台账（inv_batch）vs 单据流水推算账（入库-出库±已确认盘盈亏，红冲取反），
 *    差异批次列出差异量并定位相关单据（入库单/出库单/盘存单号），人工核查后重同步或调整
 * 说明：ERP 库存对账以回写单据的 sync_record 为准（模拟环境无独立 ERP 库存表），
 *       真实 ERP 对接后由 ERP 库存接口替换 actual 侧数据。
 */
@Slf4j
@Service
public class ReconcileService {

    /** 差异批次定位单据的最大展开数（防止极端差异爆量） */
    private static final int MAX_DIFF_ROWS = 50;

    @Resource
    private SyncRecordMapper syncRecordMapper;

    /**
     * 对账报表：{syncIssues: 同步不平清单, ledgerDiffs: 台账差异清单(含定位单据), summary: 汇总}
     */
    public Map<String, Object> reconcile() {
        Map<String, Object> result = new HashMap<>();
        result.put("syncIssues", syncIssues());
        List<Map<String, Object>> diffs = ledgerDiffs();
        result.put("ledgerDiffs", diffs);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("syncIssueCount", ((List<?>) result.get("syncIssues")).size());
        summary.put("ledgerDiffCount", diffs.size());
        result.put("summary", summary);
        return result;
    }

    /** 同步完整性：未成功（未同步/失败转人工）的同步记录 */
    public List<Map<String, Object>> syncIssues() {
        List<Map<String, Object>> rows = syncRecordMapper.queryList(
            "SELECT biz_type bizType, doc_no docNo, status, retry_count retryCount, "
                + "IFNULL(last_msg,'') lastMsg, IFNULL(DATE_FORMAT(last_time,'%Y-%m-%d %H:%i:%s'),'') lastTime "
                + "FROM sync_record WHERE status <> '成功' ORDER BY update_time DESC LIMIT 200");
        return rows == null ? new ArrayList<>() : rows;
    }

    /**
     * 台账对账：单据流水推算账（红冲取反、盘盈亏取已确认）vs inv_batch 台账现账，
     * 差异 ≠ 0 的批次输出并定位相关单据
     */
    public List<Map<String, Object>> ledgerDiffs() {
        // 推算账：入库(生效，红冲取负) - 出库(生效，红冲取负即回加) + 已确认盘盈亏
        List<Map<String, Object>> expected = syncRecordMapper.queryList(
            "SELECT t.batch_no batchNo, "
                + "IFNULL(SUM(t.qty),0) expectedQty, IFNULL(SUM(t.box),0) expectedBox FROM ( "
                + "  SELECT d.batch_no batch_no, "
                + "         SUM(CASE WHEN m.reverse_flag=1 THEN -IFNULL(d.std_weight,0) ELSE IFNULL(d.std_weight,0) END) qty, "
                + "         SUM(CASE WHEN m.reverse_flag=1 THEN -1 ELSE 1 END) box "
                + "  FROM doc_inbound_detail d JOIN doc_inbound m ON d.inbound_id=m.id "
                + "  WHERE m.status=3 AND d.batch_no IS NOT NULL AND d.batch_no<>'' GROUP BY d.batch_no "
                + "  UNION ALL "
                + "  SELECT d.batch_no, "
                + "         SUM(CASE WHEN m.reverse_flag=1 THEN IFNULL(d.std_weight,0) ELSE -IFNULL(d.std_weight,0) END), "
                + "         SUM(CASE WHEN m.reverse_flag=1 THEN IFNULL(d.boxes,0) ELSE -IFNULL(d.boxes,0) END) "
                + "  FROM doc_outbound_detail d JOIN doc_outbound m ON d.outbound_id=m.id "
                + "  WHERE m.status=3 AND d.batch_no IS NOT NULL AND d.batch_no<>'' GROUP BY d.batch_no "
                + "  UNION ALL "
                + "  SELECT d.inv_batch_no, SUM(IFNULL(d.diff_std,0)), 0 "
                + "  FROM doc_stock_count_detail d JOIN doc_stock_count m ON d.count_id=m.id "
                + "  WHERE m.status=3 AND m.adjust_state=2 AND d.inv_batch_no IS NOT NULL AND d.inv_batch_no<>'' "
                + "  GROUP BY d.inv_batch_no "
                + ") t GROUP BY t.batch_no");
        // 台账现账
        List<Map<String, Object>> actuals = syncRecordMapper.queryList(
            "SELECT inv_batch_no batchNo, IFNULL(total_qty,0) totalQty, IFNULL(total_boxes,0) totalBoxes "
                + "FROM inv_batch");
        Map<String, Map<String, Object>> actualMap = new LinkedHashMap<>();
        if (actuals != null) {
            for (Map<String, Object> row : actuals) {
                actualMap.put(str(row.get("batchNo")), row);
            }
        }
        // 台账有但单据推算没有的批次也纳入对账（异常多账）
        Map<String, Map<String, Object>> expectedMap = new LinkedHashMap<>();
        if (expected != null) {
            for (Map<String, Object> row : expected) {
                expectedMap.put(str(row.get("batchNo")), row);
            }
        }

        List<Map<String, Object>> diffs = new ArrayList<>();
        java.util.Set<String> allBatches = new java.util.LinkedHashSet<>(expectedMap.keySet());
        allBatches.addAll(actualMap.keySet());
        for (String batchNo : allBatches) {
            if (StrUtil.isEmpty(batchNo) || diffs.size() >= MAX_DIFF_ROWS) {
                continue;
            }
            Map<String, Object> exp = expectedMap.get(batchNo);
            Map<String, Object> act = actualMap.get(batchNo);
            BigDecimal expectedQty = exp == null ? BigDecimal.ZERO : dec(exp.get("expectedQty"));
            BigDecimal expectedBox = exp == null ? BigDecimal.ZERO : dec(exp.get("expectedBox"));
            BigDecimal actualQty = act == null ? BigDecimal.ZERO : dec(act.get("totalQty"));
            BigDecimal actualBox = act == null ? BigDecimal.ZERO : dec(act.get("totalBoxes"));
            BigDecimal diffQty = actualQty.subtract(expectedQty);
            BigDecimal diffBox = actualBox.subtract(expectedBox);
            if (diffQty.compareTo(BigDecimal.ZERO) == 0 && diffBox.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            Map<String, Object> diff = new LinkedHashMap<>();
            diff.put("batchNo", batchNo);
            diff.put("expectedQty", expectedQty);
            diff.put("actualQty", actualQty);
            diff.put("diffQty", diffQty);
            diff.put("expectedBox", expectedBox);
            diff.put("actualBox", actualBox);
            diff.put("diffBox", diffBox);
            diff.put("relatedDocs", relatedDocs(batchNo));
            diffs.add(diff);
        }
        return diffs;
    }

    /** 差异定位：该批次相关的 入库单/出库单/盘存单号（7.4 按单据流水定位） */
    private List<Map<String, Object>> relatedDocs(String batchNo) {
        List<Map<String, Object>> docs = new ArrayList<>();
        String safe = batchNo.replace("'", "''");
        List<Map<String, Object>> rows = syncRecordMapper.queryList(
            "SELECT '入库单' docType, m.inbound_no docNo, IFNULL(DATE_FORMAT(m.create_time,'%Y-%m-%d'),'') docDate "
                + "FROM doc_inbound m JOIN doc_inbound_detail d ON d.inbound_id=m.id "
                + "WHERE m.status=3 AND d.batch_no='" + safe + "' "
                + "UNION ALL "
                + "SELECT '出库单', m.outbound_no, IFNULL(DATE_FORMAT(m.create_time,'%Y-%m-%d'),'') "
                + "FROM doc_outbound m JOIN doc_outbound_detail d ON d.outbound_id=m.id "
                + "WHERE m.status=3 AND d.batch_no='" + safe + "' "
                + "UNION ALL "
                + "SELECT '盘存单', m.count_no, IFNULL(DATE_FORMAT(m.create_time,'%Y-%m-%d'),'') "
                + "FROM doc_stock_count m JOIN doc_stock_count_detail d ON d.count_id=m.id "
                + "WHERE m.status=3 AND d.inv_batch_no='" + safe + "' "
                + "ORDER BY docDate LIMIT 20");
        if (rows != null) {
            docs.addAll(rows);
        }
        return docs;
    }

    private static String str(Object v) {
        return v == null ? "" : v.toString();
    }

    private static BigDecimal dec(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        try {
            return new BigDecimal(v.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}

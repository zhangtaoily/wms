package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.InvBatchStock;
import com.wms.base.domain.doc.AsrsOpTask;
import com.wms.base.domain.doc.AsrsWhTask;
import com.wms.base.domain.doc.DocOutbound;
import com.wms.base.domain.doc.DocOutboundDetail;
import com.wms.base.mapper.InvBatchStockMapper;
import com.wms.base.mapper.doc.AsrsOpTaskMapper;
import com.wms.base.mapper.doc.AsrsWhTaskMapper;
import com.wms.base.mapper.doc.DocOutboundDetailMapper;
import com.wms.base.mapper.doc.DocOutboundMapper;
import com.wms.base.view.doc.DocOutboundDataView;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.exception.ServiceException;
import com.wms.common.helper.LoginHelper;
import com.wms.common.thread.ThreadPool;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.BillStatusEnum;
import com.wms.service.common.service.TableNode;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * 出库单服务：
 * 1. 生效后批次台账扣减（inv_batch/inv_batch_stock 条件扣减防超扣，箱码转已出库）
 * 2. 非红冲单按批号聚合自动生成立库仓储任务 + 作业任务（FR-10/FR-11）
 * 3. 事务提交后异步同步 ERP（幂等，以 ERP 出库单号判重，失败停留未同步可 reTry）
 * 4. 红冲（Q4）：负向单引用原单号，生效后同链路回加；原单不删不改
 */
@Slf4j
@Service
public class DocOutboundService extends AbstractM3DocService<DocOutbound, DocOutboundMapper> {

    @Resource
    private DocOutboundMapper docOutboundMapper;
    @Resource
    private DocOutboundDetailMapper docOutboundDetailMapper;
    @Resource
    private InventoryLedgerService inventoryLedgerService;
    @Resource
    private ErpOutboundSyncStub erpOutboundSyncStub;
    @Resource
    private AsrsWhTaskService asrsWhTaskService;
    @Resource
    private AsrsWhTaskMapper asrsWhTaskMapper;
    @Resource
    private AsrsOpTaskMapper asrsOpTaskMapper;
    @Resource
    private InvBatchStockMapper invBatchStockMapper;

    @Override
    public DocOutboundMapper supplierMapper() {
        return docOutboundMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(DocOutbound.class);
        TableInfo detailInfo = TableInfoHelper.getTableInfo(DocOutboundDetail.class);

        TableNode root = new TableNode();
        root.setTableClass(DocOutbound.class);
        root.setMapper(DocOutboundMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());

        TableNode detail = new TableNode();
        detail.setTableClass(DocOutboundDetail.class);
        detail.setMapper(DocOutboundDetailMapper.class);
        detail.setTableName(detailInfo.getTableName());
        detail.setPrimaryKey(detailInfo.getKeyColumn());
        detail.setForeignKey("outbound_id");
        detail.setOrderBy(" order by seq");
        detail.setParentTable(info.getTableName());

        root.addChild(detail);
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_OUTBOUND;
    }

    @Override
    protected String codeColumn() {
        return "outbound_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocOutboundDataView().fetchDataView(), pageQuery);
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        String masterTable = TableInfoHelper.getTableInfo(DocOutbound.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocOutboundDetail.class).getTableName();
        List<Map<String, Object>> masterRows = dbOldData.get(masterTable);
        List<Map<String, Object>> details = dbOldData.get(detailTable);
        if (masterRows == null || masterRows.isEmpty()) {
            throw new ServiceException("出库单数据不存在，不能生效");
        }
        if (details == null || details.isEmpty()) {
            throw new ServiceException("出库明细为空，不能生效");
        }
        Map<String, Object> master = masterRows.get(0);
        if (asInt(master.get("reverseFlag")) == 1) {
            // 红冲单校验：引用原单号必填、原单存在且已生效、红冲不可再红冲
            String sourceNo = asStr(master.get("sourceOutboundNo"));
            if (StrUtil.isEmpty(sourceNo)) {
                throw new ServiceException("红冲单必须引用原出库单号");
            }
            QueryWrapper<DocOutbound> qw = new QueryWrapper<>();
            qw.eq("outbound_no", sourceNo);
            DocOutbound source = docOutboundMapper.selectOne(qw);
            if (source == null) {
                throw new ServiceException("红冲引用的原出库单" + sourceNo + "不存在");
            }
            if (!BillStatusEnum.PASS.getCode().equals(source.getPactState())) {
                throw new ServiceException("原出库单" + sourceNo + "未生效，不能红冲");
            }
            if (source.getReverseFlag() != null && source.getReverseFlag() == 1) {
                throw new ServiceException("红冲单不可再红冲");
            }
        }
    }

    @Override
    public void afterPass(Long id, TableNode root,
                          Map<String, List<Map<String, Object>>> dbOldData,
                          Map<String, List<Map<String, Object>>> dbNewData) {
        String masterTable = TableInfoHelper.getTableInfo(DocOutbound.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocOutboundDetail.class).getTableName();
        List<Map<String, Object>> masterRows = dbNewData.get(masterTable);
        List<Map<String, Object>> details = dbNewData.get(detailTable);
        if (masterRows == null || masterRows.isEmpty()) {
            return;
        }
        Map<String, Object> master = masterRows.get(0);
        boolean reverse = asInt(master.get("reverseFlag")) == 1;
        Long warehouseId = asLong(master.get("warehouseId"));
        String warehouseName = asStr(master.get("warehouseName"));

        // 生效事务内扣账/回加：明细按批号聚合，先锁批次后扣账（7.2）
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : details) {
            groups.computeIfAbsent(asStr(row.get("batchNo")), k -> new ArrayList<>()).add(row);
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : groups.entrySet()) {
            String batchNo = entry.getKey();
            List<Map<String, Object>> rows = entry.getValue();
            String vatNo = firstStr(rows, "vatNo");
            String comp = firstStr(rows, "comp");
            String colorCode = firstStr(rows, "colorCode");
            String materialCode = firstStr(rows, "materialCode");
            int boxes = 0;
            int tubes = 0;
            int pieces = 0;
            BigDecimal stdWeight = BigDecimal.ZERO;
            BigDecimal grossWeight = BigDecimal.ZERO;
            BigDecimal netWeight = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                boxes += nvl(asInt(row.get("boxes")));
                tubes += nvl(asInt(row.get("tubes")));
                pieces += nvl(asInt(row.get("pieces")));
                stdWeight = stdWeight.add(nvl(asDecimal(row.get("stdWeight"))));
                grossWeight = grossWeight.add(nvl(asDecimal(row.get("grossWeight"))));
                netWeight = netWeight.add(nvl(asDecimal(row.get("netWeight"))));
            }
            if (reverse) {
                // 红冲回加：同入库链路累加，箱码恢复在库
                inventoryLedgerService.restoreStock(batchNo, warehouseId, warehouseName,
                    vatNo, comp, colorCode, firstStr(rows, "colorName"),
                    boxes, tubes, stdWeight, grossWeight, netWeight);
                for (Map<String, Object> row : rows) {
                    inventoryLedgerService.markBoxRestoreStock(asStr(row.get("boxCode")));
                }
            } else {
                // 扣账：逐仓位行条件扣减，任一不足抛异常整体回滚（防超扣，并发安全）
                inventoryLedgerService.deductStock(batchNo, warehouseId,
                    boxes, tubes, pieces, stdWeight, grossWeight, netWeight);
                for (Map<String, Object> row : rows) {
                    inventoryLedgerService.markBoxOutStock(asStr(row.get("boxCode")));
                }
                // 按批号聚合生成立库仓储任务 + 作业任务（FR-10/FR-11）
                createAsrsTasks(master, batchNo, materialCode, boxes);
            }
        }

        // 事务提交后异步同步 ERP：失败停留未同步（sync_status=2），可 reTry 幂等重试
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    try {
                        erpOutboundSyncStub.sync(id);
                    } catch (Exception ex) {
                        log.error("出库单{}异步同步ERP异常", id, ex);
                    }
                }, ThreadPool.fiveThreadExecutor);
            }
        });
    }

    /** 生成仓储任务（AT 号，状态直接生效）+ WCS 作业任务（执行中） */
    private void createAsrsTasks(Map<String, Object> master, String batchNo, String materialCode, int boxes) {
        Long warehouseId = asLong(master.get("warehouseId"));

        AsrsWhTask whTask = new AsrsWhTask();
        whTask.setCode(asrsWhTaskService.fetchCode(null));
        whTask.setPactState(BillStatusEnum.PASS.getCode());
        whTask.setTaskDate(LocalDate.now());
        whTask.setTaskType("条码出库");
        whTask.setExecStatus("待执行");
        whTask.setBatchNo(batchNo);
        whTask.setMaterialCode(materialCode);
        whTask.setOutboundType(asStr(master.get("outboundType")));
        whTask.setErpDocNo(asStr(master.get("erpOrderNo")));
        whTask.setErpDocType("出库单");
        whTask.setBoxes(boxes);
        whTask.setCreator(currentUser());
        whTask.setCreateTime(new Date());
        asrsWhTaskMapper.insert(whTask);

        AsrsOpTask opTask = new AsrsOpTask();
        opTask.setWcsTaskId("WCS" + System.currentTimeMillis());
        opTask.setWhTaskId(whTask.getId());
        opTask.setPactState(BillStatusEnum.PASS.getCode());
        opTask.setTaskType("条码出库");
        opTask.setOpType("出库");
        opTask.setFromLoc(selectFromLoc(batchNo, warehouseId));
        opTask.setToLoc("出口自动");
        opTask.setPort("出口自动");
        opTask.setExecStatus("执行中");
        opTask.setTaskTime(new Date());
        opTask.setCreateTime(new Date());
        asrsOpTaskMapper.insert(opTask);
    }

    /** 源仓位：取该批次×仓库现存的首行仓位（扣账不删行，仓位不变） */
    private String selectFromLoc(String batchNo, Long warehouseId) {
        QueryWrapper<InvBatchStock> qw = new QueryWrapper<>();
        qw.eq("inv_batch_no", batchNo).eq("warehouse_id", warehouseId).orderByAsc("id");
        List<InvBatchStock> rows = invBatchStockMapper.selectList(qw);
        return rows.isEmpty() ? null : rows.get(0).getLocCode();
    }

    private static String currentUser() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstStr(List<Map<String, Object>> rows, String key) {
        for (Map<String, Object> row : rows) {
            String v = asStr(row.get(key));
            if (StrUtil.isNotEmpty(v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * 重发同步（引擎 PATCH /table/reSend/{function}/{id} 入口）：id 即出库单ID
     */
    @Override
    public R reTry(Long logId) {
        Tuple2<Boolean, String> result = erpOutboundSyncStub.sync(logId);
        return result.getFrist() ? R.ok(result.getSecond()) : R.fail(result.getSecond());
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String asStr(Object v) {
        return v == null ? null : v.toString();
    }

    private static Integer asInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return new BigDecimal(v.toString()).intValue();
    }

    private static Long asLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        return new BigDecimal(v.toString()).longValue();
    }

    private static BigDecimal asDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        return new BigDecimal(v.toString());
    }
}

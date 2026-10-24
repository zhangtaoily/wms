package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.BaseWarehouse;
import com.wms.base.domain.InvBatchStock;
import com.wms.base.domain.InvBox;
import com.wms.base.domain.doc.AsrsOpTask;
import com.wms.base.domain.doc.AsrsWhTask;
import com.wms.base.domain.doc.DocStockMove;
import com.wms.base.domain.doc.DocStockMoveDetail;
import com.wms.base.mapper.BaseWarehouseMapper;
import com.wms.base.mapper.InvBatchStockMapper;
import com.wms.base.mapper.InvBoxMapper;
import com.wms.base.mapper.doc.AsrsOpTaskMapper;
import com.wms.base.mapper.doc.AsrsWhTaskMapper;
import com.wms.base.mapper.doc.DocStockMoveDetailMapper;
import com.wms.base.mapper.doc.DocStockMoveMapper;
import com.wms.base.service.BaseWmsFormService;
import com.wms.base.view.doc.DocStockMoveDataView;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.R;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.exception.ServiceException;
import com.wms.common.thread.ThreadPool;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.BillStatusEnum;
import com.wms.service.common.service.TableNode;
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
 * 移库单服务（M5 库内管理，FR-08）：
 * 1. 四种移库类型：托盘挪库/编码移库/批次移库/空托移库；明细按托盘/箱/批次层级
 * 2. Q3：仅承接同一仓库内仓位变更，跨仓移库阻断（跨仓/跨货主走出库单+入库单组合）
 * 3. 生效后台账仓位归属变更（inv_batch_stock 行间转移 + 箱码/托盘仓位更新），批次总量不变
 * 4. 立库仓移库自动生成仓储任务+作业任务（验收⑥，联动 M4 任务模型）
 * 5. 事务提交后异步同步 ERP 仓位变更（幂等，以 ERP 调拨单号判重，失败停留未同步可 reTry）
 * 6. 红冲（Q4）：反向单引用原单号，生效后按 源/目标仓位对调 执行移库；原单不删不改
 */
@Slf4j
@Service
public class DocStockMoveService extends AbstractM3DocService<DocStockMove, DocStockMoveMapper> {

    private static final String TYPE_PALLET = "托盘挪库";
    private static final String TYPE_BOX = "编码移库";
    private static final String TYPE_BATCH = "批次移库";
    private static final String TYPE_EMPTY_PALLET = "空托移库";

    @Resource
    private DocStockMoveMapper docStockMoveMapper;
    @Resource
    private DocStockMoveDetailMapper docStockMoveDetailMapper;
    @Resource
    private InventoryLedgerService inventoryLedgerService;
    @Resource
    private ErpStockMoveSyncStub erpStockMoveSyncStub;
    @Resource
    private InvBoxMapper invBoxMapper;
    @Resource
    private InvBatchStockMapper invBatchStockMapper;
    @Resource
    private BaseWarehouseMapper baseWarehouseMapper;
    @Resource
    private AsrsWhTaskService asrsWhTaskService;
    @Resource
    private AsrsWhTaskMapper asrsWhTaskMapper;
    @Resource
    private AsrsOpTaskMapper asrsOpTaskMapper;

    @Override
    public DocStockMoveMapper supplierMapper() {
        return docStockMoveMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(DocStockMove.class);
        TableInfo detailInfo = TableInfoHelper.getTableInfo(DocStockMoveDetail.class);

        TableNode root = new TableNode();
        root.setTableClass(DocStockMove.class);
        root.setMapper(DocStockMoveMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());

        TableNode detail = new TableNode();
        detail.setTableClass(DocStockMoveDetail.class);
        detail.setMapper(DocStockMoveDetailMapper.class);
        detail.setTableName(detailInfo.getTableName());
        detail.setPrimaryKey(detailInfo.getKeyColumn());
        detail.setForeignKey("move_id");
        detail.setOrderBy(" order by seq");
        detail.setParentTable(info.getTableName());

        root.addChild(detail);
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_STOCK_MOVE;
    }

    @Override
    protected String codeColumn() {
        return "move_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocStockMoveDataView().fetchDataView(), pageQuery);
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        String masterTable = TableInfoHelper.getTableInfo(DocStockMove.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocStockMoveDetail.class).getTableName();
        List<Map<String, Object>> masterRows = dbOldData.get(masterTable);
        List<Map<String, Object>> details = dbOldData.get(detailTable);
        if (masterRows == null || masterRows.isEmpty()) {
            throw new ServiceException("移库单数据不存在，不能生效");
        }
        if (details == null || details.isEmpty()) {
            throw new ServiceException("移库明细为空，不能生效");
        }
        Map<String, Object> master = masterRows.get(0);
        if (asInt(master.get("reverseFlag")) == 1) {
            // 红冲单校验：引用原单号必填、原单存在且已生效、红冲不可再红冲
            String sourceNo = asStr(master.get("sourceMoveNo"));
            if (StrUtil.isEmpty(sourceNo)) {
                throw new ServiceException("红冲单必须引用原移库单号");
            }
            QueryWrapper<DocStockMove> qw = new QueryWrapper<>();
            qw.eq("move_no", sourceNo);
            DocStockMove source = docStockMoveMapper.selectOne(qw);
            if (source == null) {
                throw new ServiceException("红冲引用的原移库单" + sourceNo + "不存在");
            }
            if (!BillStatusEnum.PASS.getCode().equals(source.getPactState())) {
                throw new ServiceException("原移库单" + sourceNo + "未生效，不能红冲");
            }
            if (source.getReverseFlag() != null && source.getReverseFlag() == 1) {
                throw new ServiceException("红冲单不可再红冲");
            }
            return;
        }
        validateMove(master, details);
    }

    /** 移库业务校验：类型必填、同仓库限制（Q3）、目标仓位必填、明细按类型校验 */
    private void validateMove(Map<String, Object> master, List<Map<String, Object>> details) {
        String moveType = asStr(master.get("moveType"));
        if (StrUtil.isEmpty(moveType)) {
            throw new ServiceException("「移库类型」为必填项");
        }
        if (!TYPE_PALLET.equals(moveType) && !TYPE_BOX.equals(moveType)
            && !TYPE_BATCH.equals(moveType) && !TYPE_EMPTY_PALLET.equals(moveType)) {
            throw new ServiceException("移库类型不合法，仅支持 托盘挪库/编码移库/批次移库/空托移库");
        }
        Long fromWarehouseId = asLong(master.get("fromWarehouseId"));
        Long toWarehouseId = asLong(master.get("toWarehouseId"));
        if (fromWarehouseId == null || toWarehouseId == null) {
            throw new ServiceException("「源仓库/目标仓库」为必填项");
        }
        // Q3：移库单仅承接同一仓库内仓位变更，跨仓/跨货主转移按 出库单+入库单 组合承接
        if (!fromWarehouseId.equals(toWarehouseId)) {
            throw new ServiceException("不支持跨仓移库：跨仓库/跨货主转移请通过 出库单+入库单 组合承接");
        }
        if (StrUtil.isEmpty(asStr(master.get("toLocCode")))) {
            throw new ServiceException("「目标仓位」为必填项");
        }
        if (StrUtil.isEmpty(asStr(master.get("fromLocCode")))) {
            throw new ServiceException("「源仓位」为必填项");
        }
        for (Map<String, Object> row : details) {
            if (TYPE_BOX.equals(moveType) && StrUtil.isEmpty(asStr(row.get("boxCode")))) {
                throw new ServiceException("编码移库明细的「箱码」为必填项");
            }
            if ((TYPE_PALLET.equals(moveType) || TYPE_EMPTY_PALLET.equals(moveType))
                && StrUtil.isEmpty(asStr(row.get("palletNo")))) {
                throw new ServiceException(moveType + "明细的「托盘号」为必填项");
            }
            if (TYPE_BATCH.equals(moveType) && StrUtil.isEmpty(asStr(row.get("batchNo")))
                && StrUtil.isEmpty(asStr(master.get("batchNo")))) {
                throw new ServiceException("批次移库的「批号」为必填项");
            }
        }
    }

    @Override
    public void afterPass(Long id, TableNode root,
                          Map<String, List<Map<String, Object>>> dbOldData,
                          Map<String, List<Map<String, Object>>> dbNewData) {
        String masterTable = TableInfoHelper.getTableInfo(DocStockMove.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocStockMoveDetail.class).getTableName();
        List<Map<String, Object>> masterRows = dbNewData.get(masterTable);
        List<Map<String, Object>> details = dbNewData.get(detailTable);
        if (masterRows == null || masterRows.isEmpty()) {
            return;
        }
        Map<String, Object> master = masterRows.get(0);
        boolean reverse = asInt(master.get("reverseFlag")) == 1;
        String moveType = asStr(master.get("moveType"));

        // 红冲（Q4）：按 源/目标仓位对调 执行反向移库，总量仍不变
        Long warehouseId = reverse ? asLong(master.get("toWarehouseId")) : asLong(master.get("fromWarehouseId"));
        String fromLoc = reverse ? asStr(master.get("toLocCode")) : asStr(master.get("fromLocCode"));
        String toLoc = reverse ? asStr(master.get("fromLocCode")) : asStr(master.get("toLocCode"));
        String warehouseName = null;
        BaseWarehouse warehouse = baseWarehouseMapper.selectById(warehouseId);
        if (warehouse != null) {
            warehouseName = warehouse.getWarehouseName();
        }

        switch (moveType == null ? "" : moveType) {
            case TYPE_EMPTY_PALLET:
                // 空托移库：仅托盘台账仓位变更，无库存数量
                for (Map<String, Object> row : details) {
                    inventoryLedgerService.movePalletLoc(asStr(row.get("palletNo")), toLoc);
                }
                break;
            case TYPE_PALLET:
                // 托盘挪库：整托箱码随托迁移，按 箱码当前仓位×批次 聚合转移仓位账
                for (Map<String, Object> row : details) {
                    movePallet(row, warehouseId, warehouseName, toLoc);
                }
                break;
            case TYPE_BOX:
                // 编码移库：逐箱转移，按箱码台账数量搬运
                for (Map<String, Object> row : details) {
                    moveBox(row, master, warehouseId, warehouseName, fromLoc, toLoc);
                }
                break;
            case TYPE_BATCH:
                // 批次移库：整批在源仓位的台账行整体迁移
                moveBatch(master, details, warehouseId, warehouseName, fromLoc, toLoc);
                break;
            default:
                throw new ServiceException("移库类型不合法，不能生效");
        }

        // 立库仓移库生成仓储任务+作业任务（验收⑥：立库移库由仓储任务执行）
        if (warehouse != null && BaseWmsFormService.WH_TYPE_LIKU.equals(warehouse.getWhType())
            && !TYPE_EMPTY_PALLET.equals(moveType)) {
            createAsrsMoveTasks(master, details);
        }

        // 事务提交后异步同步 ERP 仓位变更：失败停留未同步（sync_status=2），可 reTry 幂等重试
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    try {
                        erpStockMoveSyncStub.sync(id);
                    } catch (Exception ex) {
                        log.error("移库单{}异步同步ERP异常", id, ex);
                    }
                }, ThreadPool.fiveThreadExecutor);
            }
        });
    }

    /** 托盘挪库：托下在库箱码按 批次×当前仓位 聚合转移仓位账，箱码/托盘仓位更新 */
    private void movePallet(Map<String, Object> row, Long warehouseId, String warehouseName, String toLoc) {
        String palletNo = asStr(row.get("palletNo"));
        if (StrUtil.isEmpty(palletNo)) {
            return;
        }
        QueryWrapper<InvBox> boxQw = new QueryWrapper<>();
        boxQw.eq("pallet_no", palletNo).eq("status", "在库");
        List<InvBox> boxes = invBoxMapper.selectList(boxQw);
        // 聚合键：批号×箱码当前仓位（箱码仓位缺省回退单头源仓位）
        Map<String, List<InvBox>> groups = new LinkedHashMap<>();
        for (InvBox box : boxes) {
            String fromLoc = StrUtil.isNotEmpty(box.getLocCode()) ? box.getLocCode() : "";
            groups.computeIfAbsent(asStr(box.getBatchNo()) + "|" + fromLoc, k -> new ArrayList<>()).add(box);
        }
        for (List<InvBox> group : groups.values()) {
            String batchNo = group.get(0).getBatchNo();
            String fromLoc = StrUtil.isNotEmpty(group.get(0).getLocCode())
                ? group.get(0).getLocCode() : null;
            if (fromLoc == null) {
                continue;
            }
            int tubes = 0;
            BigDecimal stdWeight = BigDecimal.ZERO;
            BigDecimal grossWeight = BigDecimal.ZERO;
            BigDecimal netWeight = BigDecimal.ZERO;
            for (InvBox box : group) {
                tubes += box.getTubes() == null ? 0 : box.getTubes();
                stdWeight = stdWeight.add(box.getStdWeight() == null ? BigDecimal.ZERO : box.getStdWeight());
                grossWeight = grossWeight.add(box.getGrossWeight() == null ? BigDecimal.ZERO : box.getGrossWeight());
                netWeight = netWeight.add(box.getNetWeight() == null ? BigDecimal.ZERO : box.getNetWeight());
            }
            inventoryLedgerService.moveStock(batchNo, warehouseId, warehouseName, fromLoc, toLoc,
                group.size(), tubes, group.size(), stdWeight, grossWeight, netWeight);
            for (InvBox box : group) {
                inventoryLedgerService.moveBoxLoc(box.getBoxCode(), toLoc, warehouseName);
            }
        }
        inventoryLedgerService.movePalletLoc(palletNo, toLoc);
    }

    /** 编码移库：单箱按箱码台账数量转移仓位账，箱码仓位更新 */
    private void moveBox(Map<String, Object> row, Map<String, Object> master,
                         Long warehouseId, String warehouseName, String masterFromLoc, String masterToLoc) {
        String boxCode = asStr(row.get("boxCode"));
        if (StrUtil.isEmpty(boxCode)) {
            return;
        }
        QueryWrapper<InvBox> qw = new QueryWrapper<>();
        qw.eq("box_code", boxCode);
        InvBox box = invBoxMapper.selectOne(qw);
        if (box == null) {
            throw new ServiceException("箱码" + boxCode + "不存在，移库失败");
        }
        if (!"在库".equals(box.getStatus())) {
            throw new ServiceException("箱码" + boxCode + "不在库（当前状态：" + box.getStatus() + "），不能移库");
        }
        String fromLoc = StrUtil.isNotEmpty(box.getLocCode()) ? box.getLocCode() : masterFromLoc;
        String toLoc = StrUtil.isNotEmpty(asStr(row.get("toLocCode"))) ? asStr(row.get("toLocCode")) : masterToLoc;
        inventoryLedgerService.moveStock(box.getBatchNo(), warehouseId, warehouseName, fromLoc, toLoc,
            1, box.getTubes() == null ? 0 : box.getTubes(), 1,
            box.getStdWeight() == null ? BigDecimal.ZERO : box.getStdWeight(),
            box.getGrossWeight() == null ? BigDecimal.ZERO : box.getGrossWeight(),
            box.getNetWeight() == null ? BigDecimal.ZERO : box.getNetWeight());
        inventoryLedgerService.moveBoxLoc(boxCode, toLoc, warehouseName);
    }

    /** 批次移库：整批在源仓位的仓位账行整体迁移，批下在库箱码随迁 */
    private void moveBatch(Map<String, Object> master, List<Map<String, Object>> details,
                           Long warehouseId, String warehouseName, String fromLoc, String toLoc) {
        List<String> batchNos = new ArrayList<>();
        for (Map<String, Object> row : details) {
            String batchNo = asStr(row.get("batchNo"));
            if (StrUtil.isNotEmpty(batchNo) && !batchNos.contains(batchNo)) {
                batchNos.add(batchNo);
            }
        }
        String masterBatch = asStr(master.get("batchNo"));
        if (batchNos.isEmpty() && StrUtil.isNotEmpty(masterBatch)) {
            batchNos.add(masterBatch);
        }
        if (batchNos.isEmpty()) {
            throw new ServiceException("批次移库的「批号」为必填项");
        }
        for (String batchNo : batchNos) {
            QueryWrapper<InvBatchStock> qw = new QueryWrapper<>();
            qw.eq("inv_batch_no", batchNo).eq("warehouse_id", warehouseId);
            if (StrUtil.isNotEmpty(fromLoc)) {
                qw.eq("loc_code", fromLoc);
            }
            List<InvBatchStock> rows = invBatchStockMapper.selectList(qw);
            if (rows.isEmpty()) {
                throw new ServiceException("批次" + batchNo + "在源仓位无库存台账，移库失败");
            }
            for (InvBatchStock stock : rows) {
                String srcLoc = StrUtil.isNotEmpty(stock.getLocCode()) ? stock.getLocCode() : fromLoc;
                inventoryLedgerService.moveStock(batchNo, warehouseId, warehouseName, srcLoc, toLoc,
                    stock.getBoxes(), stock.getTubes(), stock.getPieces(),
                    stock.getStdWeight(), stock.getGrossWeight(), stock.getNetWeight());
                // 批下在库箱码随批迁移
                QueryWrapper<InvBox> boxQw = new QueryWrapper<>();
                boxQw.eq("batch_no", batchNo).eq("loc_code", srcLoc).eq("status", "在库");
                List<InvBox> boxes = invBoxMapper.selectList(boxQw);
                for (InvBox box : boxes) {
                    inventoryLedgerService.moveBoxLoc(box.getBoxCode(), toLoc, warehouseName);
                }
            }
        }
    }

    /** 立库移库任务：仓储任务（移库，状态生效）+ WCS 作业任务（执行中） */
    private void createAsrsMoveTasks(Map<String, Object> master, List<Map<String, Object>> details) {
        String firstPalletNo = null;
        if (details != null) {
            for (Map<String, Object> row : details) {
                String palletNo = asStr(row.get("palletNo"));
                if (StrUtil.isNotEmpty(palletNo)) {
                    firstPalletNo = palletNo;
                    break;
                }
            }
        }
        AsrsWhTask whTask = new AsrsWhTask();
        whTask.setCode(asrsWhTaskService.fetchCode(null));
        whTask.setPactState(BillStatusEnum.PASS.getCode());
        whTask.setTaskDate(LocalDate.now());
        whTask.setTaskType("移库");
        whTask.setExecStatus("待执行");
        whTask.setBatchNo(asStr(master.get("batchNo")));
        whTask.setErpDocNo(asStr(master.get("code")));
        whTask.setErpDocType("移库单");
        asrsWhTaskMapper.insert(whTask);

        AsrsOpTask opTask = new AsrsOpTask();
        opTask.setWcsTaskId("WCS" + System.currentTimeMillis());
        opTask.setWhTaskId(whTask.getId());
        opTask.setPactState(BillStatusEnum.PASS.getCode());
        opTask.setTaskType("移库");
        opTask.setOpType("移库");
        opTask.setPalletNo(firstPalletNo);
        opTask.setFromLoc(asStr(master.get("fromLocCode")));
        opTask.setToLoc(asStr(master.get("toLocCode")));
        opTask.setExecStatus("执行中");
        opTask.setTaskTime(new Date());
        opTask.setCreateTime(new Date());
        asrsOpTaskMapper.insert(opTask);
    }

    /**
     * 重发同步（引擎 PATCH /table/reSend/{function}/{id} 入口）：id 即移库单ID
     */
    @Override
    public R reTry(Long logId) {
        Tuple2<Boolean, String> result = erpStockMoveSyncStub.sync(logId);
        return result.getFrist() ? R.ok(result.getSecond()) : R.fail(result.getSecond());
    }

    private String asStr(Object v) {
        return v == null ? null : v.toString();
    }

    private Integer asInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return new BigDecimal(v.toString()).intValue();
    }

    private Long asLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        return new BigDecimal(v.toString()).longValue();
    }
}

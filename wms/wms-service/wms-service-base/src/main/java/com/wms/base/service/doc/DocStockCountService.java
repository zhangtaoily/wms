package com.wms.base.service.doc;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocStockCount;
import com.wms.base.domain.doc.DocStockCountBox;
import com.wms.base.domain.doc.DocStockCountDetail;
import com.wms.base.mapper.doc.DocStockCountBoxMapper;
import com.wms.base.mapper.doc.DocStockCountDetailMapper;
import com.wms.base.mapper.doc.DocStockCountMapper;
import com.wms.base.view.doc.DocStockCountDataView;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * 盘存单服务（M5 库内管理，FR-09）：
 * 1. 生效前按明细行计算盈亏（盈亏=实盘-盘前，筒数/标重/毛重/净重）
 * 2. Q2：生效后生成盘盈亏调整记录（adjust_state=待确认），台账不在生效时调整；
 *    仓库主管确认（confirmAdjust）通过后调整库存批次台账并回写 ERP 数量变更，驳回不调整
 * 3. 无盈亏的盘存单不生成调整记录，无需确认
 */
@Slf4j
@Service
public class DocStockCountService extends AbstractM3DocService<DocStockCount, DocStockCountMapper> {

    /** 盘盈亏调整确认状态 */
    public static final int ADJUST_WAIT = 1;
    public static final int ADJUST_CONFIRMED = 2;
    public static final int ADJUST_REJECTED = 3;

    @Resource
    private DocStockCountMapper docStockCountMapper;
    @Resource
    private DocStockCountDetailMapper docStockCountDetailMapper;
    @Resource
    private InventoryLedgerService inventoryLedgerService;
    @Resource
    private ErpStockCountSyncStub erpStockCountSyncStub;

    @Override
    public DocStockCountMapper supplierMapper() {
        return docStockCountMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(DocStockCount.class);
        TableInfo detailInfo = TableInfoHelper.getTableInfo(DocStockCountDetail.class);
        TableInfo boxInfo = TableInfoHelper.getTableInfo(DocStockCountBox.class);

        TableNode root = new TableNode();
        root.setTableClass(DocStockCount.class);
        root.setMapper(DocStockCountMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());

        TableNode detail = new TableNode();
        detail.setTableClass(DocStockCountDetail.class);
        detail.setMapper(DocStockCountDetailMapper.class);
        detail.setTableName(detailInfo.getTableName());
        detail.setPrimaryKey(detailInfo.getKeyColumn());
        detail.setForeignKey("count_id");
        detail.setOrderBy(" order by seq");
        detail.setParentTable(info.getTableName());

        // 孙表：逐箱称重记录（引擎递归加载/保存，外键指向明细行）
        TableNode box = new TableNode();
        box.setTableClass(DocStockCountBox.class);
        box.setMapper(DocStockCountBoxMapper.class);
        box.setTableName(boxInfo.getTableName());
        box.setPrimaryKey(boxInfo.getKeyColumn());
        box.setForeignKey("count_detail_id");
        box.setParentTable(detailInfo.getTableName());
        detail.addChild(box);

        root.addChild(detail);
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_STOCK_COUNT;
    }

    @Override
    protected String codeColumn() {
        return "count_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocStockCountDataView().fetchDataView(), pageQuery);
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        String masterTable = TableInfoHelper.getTableInfo(DocStockCount.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocStockCountDetail.class).getTableName();
        List<Map<String, Object>> masterRows = dbOldData.get(masterTable);
        List<Map<String, Object>> details = dbOldData.get(detailTable);
        if (masterRows == null || masterRows.isEmpty()) {
            throw new ServiceException("盘存单数据不存在，不能生效");
        }
        Map<String, Object> master = masterRows.get(0);
        if (asLong(master.get("warehouseId")) == null) {
            throw new ServiceException("「仓库」为必填项");
        }
        if (details == null || details.isEmpty()) {
            throw new ServiceException("盘存明细为空，不能生效");
        }
        // 逐行计算盈亏（盈亏=实盘-盘前），随生效落库（验收④：盈亏明细正确）
        for (Map<String, Object> row : details) {
            Integer diffTubes = intDiff(row.get("realTubes"), row.get("preTubes"));
            BigDecimal diffStd = decDiff(row.get("realStd"), row.get("preStd"));
            BigDecimal diffGross = decDiff(row.get("realGross"), row.get("preGross"));
            BigDecimal diffNet = decDiff(row.get("realNet"), row.get("preNet"));
            DocStockCountDetail upd = new DocStockCountDetail();
            upd.setId(asLong(row.get("id")));
            upd.setDiffTubes(diffTubes);
            upd.setDiffStd(diffStd);
            upd.setDiffGross(diffGross);
            upd.setDiffNet(diffNet);
            docStockCountDetailMapper.updateById(upd);
        }
    }

    @Override
    public void afterPass(Long id, TableNode root,
                          Map<String, List<Map<String, Object>>> dbOldData,
                          Map<String, List<Map<String, Object>>> dbNewData) {
        String detailTable = TableInfoHelper.getTableInfo(DocStockCountDetail.class).getTableName();
        List<Map<String, Object>> details = dbNewData.get(detailTable);
        boolean hasVariance = false;
        if (details != null) {
            for (Map<String, Object> row : details) {
                if (intDiff(row.get("realTubes"), row.get("preTubes")) != 0
                    || decDiff(row.get("realStd"), row.get("preStd")).compareTo(BigDecimal.ZERO) != 0
                    || decDiff(row.get("realGross"), row.get("preGross")).compareTo(BigDecimal.ZERO) != 0
                    || decDiff(row.get("realNet"), row.get("preNet")).compareTo(BigDecimal.ZERO) != 0) {
                    hasVariance = true;
                    break;
                }
            }
        }
        if (!hasVariance) {
            return;
        }
        // Q2：生效后生成盘盈亏调整记录（待确认），主管确认后方可调整台账与回写 ERP
        UpdateWrapper<DocStockCount> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("adjust_state", ADJUST_WAIT).set("update_time", new Date());
        docStockCountMapper.update(null, uw);
    }

    /**
     * 盘盈亏主管确认（Q2，验收⑤）：仓库主管放行/驳回。
     * 放行：事务内按盈亏增量调整库存批次台账，事务提交后异步回写 ERP 数量变更；
     * 驳回：仅记录状态，台账不变。
     *
     * @param id      盘存单ID
     * @param approved true=放行 false=驳回
     * @param remark  确认/驳回备注
     */
    @Transactional(rollbackFor = Exception.class)
    public Tuple2<Boolean, String> confirmAdjust(Long id, boolean approved, String remark) {
        DocStockCount doc = docStockCountMapper.selectById(id);
        if (doc == null) {
            return Tuple2.of(false, "盘存单不存在");
        }
        if (!BillStatusEnum.PASS.getCode().equals(doc.getPactState())) {
            return Tuple2.of(false, "盘存单未生效，无需确认");
        }
        if (doc.getAdjustState() == null || doc.getAdjustState() != ADJUST_WAIT) {
            return Tuple2.of(false, "该盘存单无待确认的盘盈亏调整");
        }
        if (!approved) {
            // 驳回：调整记录置已驳回，台账不变；原因追加至备注留痕
            DocStockCount upd = new DocStockCount();
            upd.setId(id);
            upd.setAdjustState(ADJUST_REJECTED);
            upd.setAdjustConfirmBy(currentUser());
            upd.setAdjustConfirmTime(java.time.LocalDateTime.now());
            if (StrUtil.isNotEmpty(remark)) {
                upd.setRemark(appendRemark(doc.getRemark(), "驳回原因：" + remark));
            }
            upd.setUpdateTime(new Date());
            docStockCountMapper.updateById(upd);
            return Tuple2.of(true, "盘盈亏调整已驳回");
        }

        // 放行：按明细盈亏增量调整台账（仓位行优先取明细仓位，缺省回退单头仓位）
        DocStockCount reload = docStockCountMapper.selectById(id);
        QueryWrapper<DocStockCountDetail> qw = new QueryWrapper<>();
        qw.eq("count_id", id).orderByAsc("seq");
        List<DocStockCountDetail> details = docStockCountDetailMapper.selectList(qw);
        for (DocStockCountDetail row : details) {
            if (row.getDiffTubes() == null && row.getDiffStd() == null
                && row.getDiffGross() == null && row.getDiffNet() == null) {
                continue;
            }
            inventoryLedgerService.adjustStock(
                row.getInvBatchNo(), row.getVatNo(), row.getComp(), row.getColorCode(), row.getColorName(),
                reload.getWarehouseId(), reload.getWarehouseName(),
                StrUtil.isNotEmpty(row.getLocCode()) ? row.getLocCode() : reload.getLocCode(),
                row.getDiffTubes() == null ? 0 : row.getDiffTubes(),
                row.getDiffStd(), row.getDiffGross(), row.getDiffNet());
        }
        DocStockCount upd = new DocStockCount();
        upd.setId(id);
        upd.setAdjustState(ADJUST_CONFIRMED);
        upd.setAdjustConfirmBy(currentUser());
        upd.setAdjustConfirmTime(java.time.LocalDateTime.now());
        if (StrUtil.isNotEmpty(remark)) {
            upd.setRemark(appendRemark(reload.getRemark(), remark));
        }
        upd.setUpdateTime(new Date());
        docStockCountMapper.updateById(upd);

        // 事务提交后异步回写 ERP 数量变更：失败停留未同步（sync_status=2），可 reTry 幂等重试
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    try {
                        erpStockCountSyncStub.sync(id);
                    } catch (Exception ex) {
                        log.error("盘存单{}异步同步ERP异常", id, ex);
                    }
                }, ThreadPool.fiveThreadExecutor);
            }
        });
        return Tuple2.of(true, "盘盈亏调整已确认，库存台账已校平并同步ERP");
    }

    /**
     * 重发同步（引擎 PATCH /table/reSend/{function}/{id} 入口）：id 即盘存单ID
     */
    @Override
    public R reTry(Long logId) {
        Tuple2<Boolean, String> result = erpStockCountSyncStub.sync(logId);
        return result.getFrist() ? R.ok(result.getSecond()) : R.fail(result.getSecond());
    }

    private static String appendRemark(String origin, String addition) {
        String text = addition == null ? "" : addition;
        return StrUtil.isEmpty(origin) ? text : origin + "；" + text;
    }

    private static String currentUser() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception e) {
            return null;
        }
    }

    private Integer intDiff(Object real, Object pre) {
        Integer r = asInt(real);
        Integer p = asInt(pre);
        int rv = r == null ? 0 : r;
        int pv = p == null ? 0 : p;
        return rv - pv;
    }

    private BigDecimal decDiff(Object real, Object pre) {
        BigDecimal r = asDecimal(real);
        BigDecimal p = asDecimal(pre);
        return (r == null ? BigDecimal.ZERO : r).subtract(p == null ? BigDecimal.ZERO : p);
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

    private BigDecimal asDecimal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        return new BigDecimal(v.toString());
    }
}

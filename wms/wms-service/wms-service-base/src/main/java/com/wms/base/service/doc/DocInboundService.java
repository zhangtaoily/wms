package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocInbound;
import com.wms.base.domain.doc.DocInboundDetail;
import com.wms.base.mapper.doc.DocInboundDetailMapper;
import com.wms.base.mapper.doc.DocInboundMapper;
import com.wms.base.view.doc.DocInboundDataView;
import com.wms.common.api.BasicBO;
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
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * 入库单服务：
 * 1. 生效后批次台账增加（inv_batch/inv_batch_stock 累加，箱码转在库）
 * 2. 事务提交后异步同步 ERP（幂等，以 ERP 入库单号判重，失败停留未同步可 reTry）
 * 3. 红冲（Q4）：负向单引用原单号，生效后同链路累减；原单不删不改
 */
@Slf4j
@Service
public class DocInboundService extends AbstractM3DocService<DocInbound, DocInboundMapper> {

    @Resource
    private DocInboundMapper docInboundMapper;
    @Resource
    private DocInboundDetailMapper docInboundDetailMapper;
    @Resource
    private InventoryLedgerService inventoryLedgerService;
    @Resource
    private ErpInboundSyncStub erpInboundSyncStub;

    @Override
    public DocInboundMapper supplierMapper() {
        return docInboundMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(DocInbound.class);
        TableInfo detailInfo = TableInfoHelper.getTableInfo(DocInboundDetail.class);

        TableNode root = new TableNode();
        root.setTableClass(DocInbound.class);
        root.setMapper(DocInboundMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());

        TableNode detail = new TableNode();
        detail.setTableClass(DocInboundDetail.class);
        detail.setMapper(DocInboundDetailMapper.class);
        detail.setTableName(detailInfo.getTableName());
        detail.setPrimaryKey(detailInfo.getKeyColumn());
        detail.setForeignKey("inbound_id");
        detail.setOrderBy(" order by seq");
        detail.setParentTable(info.getTableName());

        root.addChild(detail);
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_INBOUND;
    }

    @Override
    protected String codeColumn() {
        return "inbound_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocInboundDataView().fetchDataView(), pageQuery);
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        String masterTable = TableInfoHelper.getTableInfo(DocInbound.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocInboundDetail.class).getTableName();
        List<Map<String, Object>> masterRows = dbOldData.get(masterTable);
        List<Map<String, Object>> details = dbOldData.get(detailTable);
        if (masterRows == null || masterRows.isEmpty()) {
            throw new ServiceException("入库单数据不存在，不能生效");
        }
        if (details == null || details.isEmpty()) {
            throw new ServiceException("入库明细为空，不能生效");
        }
        Map<String, Object> master = masterRows.get(0);
        if (asInt(master.get("reverseFlag")) == 1) {
            // 红冲单校验：引用原单号必填、原单存在且已生效、红冲不可再红冲
            String sourceNo = asStr(master.get("sourceInboundNo"));
            if (StrUtil.isEmpty(sourceNo)) {
                throw new ServiceException("红冲单必须引用原入库单号");
            }
            QueryWrapper<DocInbound> qw = new QueryWrapper<>();
            qw.eq("inbound_no", sourceNo);
            DocInbound source = docInboundMapper.selectOne(qw);
            if (source == null) {
                throw new ServiceException("红冲引用的原入库单" + sourceNo + "不存在");
            }
            if (!BillStatusEnum.PASS.getCode().equals(source.getPactState())) {
                throw new ServiceException("原入库单" + sourceNo + "未生效，不能红冲");
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
        String masterTable = TableInfoHelper.getTableInfo(DocInbound.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocInboundDetail.class).getTableName();
        List<Map<String, Object>> masterRows = dbNewData.get(masterTable);
        List<Map<String, Object>> details = dbNewData.get(detailTable);
        if (masterRows == null || masterRows.isEmpty()) {
            return;
        }
        Map<String, Object> master = masterRows.get(0);
        boolean reverse = asInt(master.get("reverseFlag")) == 1;
        Long warehouseId = asLong(master.get("warehouseId"));
        String warehouseName = asStr(master.get("warehouseName"));

        // 生效事务内写批次台账：明细按 批号+库位 聚合，红冲全量取负同链路累减
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : details) {
            String key = asStr(row.get("batchNo")) + "|" + asStr(row.get("locCode"));
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        int sign = reverse ? -1 : 1;
        for (List<Map<String, Object>> rows : groups.values()) {
            String batchNo = null;
            String vatNo = null;
            String comp = null;
            String colorCode = null;
            String colorName = null;
            String locCode = null;
            int tubes = 0;
            BigDecimal stdWeight = BigDecimal.ZERO;
            BigDecimal grossWeight = BigDecimal.ZERO;
            BigDecimal netWeight = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                if (batchNo == null) {
                    batchNo = asStr(row.get("batchNo"));
                }
                if (vatNo == null) {
                    vatNo = asStr(row.get("vatNo"));
                }
                if (comp == null) {
                    comp = asStr(row.get("comp"));
                }
                if (colorCode == null) {
                    colorCode = asStr(row.get("colorCode"));
                }
                if (colorName == null) {
                    colorName = asStr(row.get("colorName"));
                }
                if (locCode == null) {
                    locCode = asStr(row.get("locCode"));
                }
                Integer rowTubes = asInt(row.get("tubes"));
                tubes += rowTubes == null ? 0 : rowTubes;
                stdWeight = stdWeight.add(nvl(asDecimal(row.get("stdWeight"))));
                grossWeight = grossWeight.add(nvl(asDecimal(row.get("grossWeight"))));
                netWeight = netWeight.add(nvl(asDecimal(row.get("netWeight"))));
            }
            inventoryLedgerService.inboundStock(
                batchNo, vatNo, comp, colorCode, colorName,
                warehouseId, warehouseName, locCode,
                sign * rows.size(), sign * tubes,
                stdWeight.multiply(BigDecimal.valueOf(sign)),
                grossWeight.multiply(BigDecimal.valueOf(sign)),
                netWeight.multiply(BigDecimal.valueOf(sign)));
            if (!reverse) {
                for (Map<String, Object> row : rows) {
                    inventoryLedgerService.markBoxInStock(asStr(row.get("boxCode")), locCode, warehouseName);
                }
            }
        }

        // 事务提交后异步同步 ERP：失败停留未同步（sync_status=2），可 reTry 幂等重试
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    try {
                        erpInboundSyncStub.sync(id);
                    } catch (Exception ex) {
                        log.error("入库单{}异步同步ERP异常", id, ex);
                    }
                }, ThreadPool.fiveThreadExecutor);
            }
        });
    }

    /**
     * 重发同步（引擎 PATCH /table/reSend/{function}/{id} 入口）：id 即入库单ID
     */
    @Override
    public R reTry(Long logId) {
        Tuple2<Boolean, String> result = erpInboundSyncStub.sync(logId);
        return result.getFrist() ? R.ok(result.getSecond()) : R.fail(result.getSecond());
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
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

package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocPalletOrder;
import com.wms.base.domain.doc.DocPalletOrderDetail;
import com.wms.base.domain.doc.DocPalletOrderPallet;
import com.wms.base.mapper.doc.DocPalletOrderDetailMapper;
import com.wms.base.mapper.doc.DocPalletOrderMapper;
import com.wms.base.mapper.doc.DocPalletOrderPalletMapper;
import com.wms.base.view.doc.DocPalletOrderDataView;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.exception.ServiceException;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 组托单服务：三种组托方式，生效后生成托盘台账（inv_pallet）并归属箱码
 * 结构：root(doc_pallet_order) + 平挂两子表（托盘组 / 箱明细），箱明细以 palletNo 关联托盘
 */
@Service
public class DocPalletOrderService extends AbstractM3DocService<DocPalletOrder, DocPalletOrderMapper> {

    @Resource
    private DocPalletOrderMapper docPalletOrderMapper;
    @Resource
    private DocPalletOrderPalletMapper docPalletOrderPalletMapper;
    @Resource
    private DocPalletOrderDetailMapper docPalletOrderDetailMapper;
    @Resource
    private InventoryLedgerService inventoryLedgerService;

    @Override
    public DocPalletOrderMapper supplierMapper() {
        return docPalletOrderMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(DocPalletOrder.class);
        TableInfo palletInfo = TableInfoHelper.getTableInfo(DocPalletOrderPallet.class);
        TableInfo detailInfo = TableInfoHelper.getTableInfo(DocPalletOrderDetail.class);

        TableNode root = new TableNode();
        root.setTableClass(DocPalletOrder.class);
        root.setMapper(DocPalletOrderMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());

        TableNode pallet = new TableNode();
        pallet.setTableClass(DocPalletOrderPallet.class);
        pallet.setMapper(DocPalletOrderPalletMapper.class);
        pallet.setTableName(palletInfo.getTableName());
        pallet.setPrimaryKey(palletInfo.getKeyColumn());
        pallet.setForeignKey("order_id");
        pallet.setOrderBy(" order by seq");

        TableNode detail = new TableNode();
        detail.setTableClass(DocPalletOrderDetail.class);
        detail.setMapper(DocPalletOrderDetailMapper.class);
        detail.setTableName(detailInfo.getTableName());
        detail.setPrimaryKey(detailInfo.getKeyColumn());
        detail.setForeignKey("order_id");
        detail.setOrderBy(" order by seq");
        pallet.setParentTable(info.getTableName());
        detail.setParentTable(info.getTableName());

        root.addChild(pallet);
        root.addChild(detail);
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_PALLET_ORDER;
    }

    @Override
    protected String codeColumn() {
        return "pallet_order_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocPalletOrderDataView().fetchDataView(), pageQuery);
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        String palletTable = TableInfoHelper.getTableInfo(DocPalletOrderPallet.class).getTableName();
        String detailTable = TableInfoHelper.getTableInfo(DocPalletOrderDetail.class).getTableName();
        List<Map<String, Object>> pallets = dbOldData.get(palletTable);
        List<Map<String, Object>> details = dbOldData.get(detailTable);
        if (pallets == null || pallets.isEmpty()) {
            throw new ServiceException("托盘组为空，不能生效");
        }
        if (details == null || details.isEmpty()) {
            throw new ServiceException("箱明细为空，不能生效");
        }
        // 同托箱码归属一致：同一托盘号的箱明细必须同批号
        Map<String, String> palletBatch = new LinkedHashMap<>();
        for (Map<String, Object> row : details) {
            String palletNo = asStr(row.get("palletNo"));
            if (StrUtil.isEmpty(palletNo)) {
                throw new ServiceException("存在托盘号为空的箱明细行，不能生效");
            }
            if (StrUtil.isEmpty(asStr(row.get("boxCode")))) {
                throw new ServiceException("存在箱码为空的明细行，不能生效");
            }
            String batchNo = asStr(row.get("batchNo"));
            String exists = palletBatch.get(palletNo);
            if (exists == null) {
                palletBatch.put(palletNo, batchNo);
            } else if (!equalsStr(exists, batchNo)) {
                throw new ServiceException("托盘" + palletNo + "的箱明细批号不一致，同托箱码归属必须一致");
            }
        }
        // 托盘组里声明的托盘号必须在箱明细中出现（防呆：托盘无箱不许生效）
        for (Map<String, Object> row : pallets) {
            String palletNo = asStr(row.get("palletNo"));
            if (StrUtil.isEmpty(palletNo)) {
                throw new ServiceException("存在托盘号为空的托盘组行，不能生效");
            }
            if (!palletBatch.containsKey(palletNo)) {
                throw new ServiceException("托盘" + palletNo + "未组入任何箱码，不能生效");
            }
        }
    }

    @Override
    public void afterPass(Long id, TableNode root,
                          Map<String, List<Map<String, Object>>> dbOldData,
                          Map<String, List<Map<String, Object>>> dbNewData) {
        // 生效事务内写托盘台账：箱明细按托盘号分组，逐托 palletize（幂等累加）
        String detailTable = TableInfoHelper.getTableInfo(DocPalletOrderDetail.class).getTableName();
        List<Map<String, Object>> details = dbNewData.get(detailTable);
        if (details == null) {
            return;
        }
        Map<String, Object> master = dbNewData.get(TableInfoHelper.getTableInfo(DocPalletOrder.class).getTableName()) == null
            ? null : dbNewData.get(TableInfoHelper.getTableInfo(DocPalletOrder.class).getTableName()).get(0);
        String masterBatchNo = master == null ? null : asStr(master.get("batchNo"));

        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : details) {
            String palletNo = asStr(row.get("palletNo"));
            if (StrUtil.isEmpty(palletNo)) {
                continue;
            }
            groups.computeIfAbsent(palletNo, k -> new ArrayList<>()).add(row);
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : groups.entrySet()) {
            List<Map<String, Object>> rows = entry.getValue();
            List<String> boxCodes = new ArrayList<>(rows.size());
            BigDecimal totalWeight = BigDecimal.ZERO;
            String groupBatchNo = null;
            for (Map<String, Object> row : rows) {
                boxCodes.add(asStr(row.get("boxCode")));
                BigDecimal stdWeight = asDecimal(row.get("stdWeight"));
                if (stdWeight != null) {
                    totalWeight = totalWeight.add(stdWeight);
                }
                if (groupBatchNo == null && StrUtil.isNotEmpty(asStr(row.get("batchNo")))) {
                    groupBatchNo = asStr(row.get("batchNo"));
                }
            }
            inventoryLedgerService.palletize(
                entry.getKey(),
                groupBatchNo != null ? groupBatchNo : masterBatchNo,
                rows.size(),
                totalWeight,
                boxCodes);
        }
    }

    private boolean equalsStr(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private String asStr(Object v) {
        return v == null ? null : v.toString();
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

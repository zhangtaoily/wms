package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocCodeCollect;
import com.wms.base.domain.doc.DocCodeCollectDetail;
import com.wms.base.mapper.doc.DocCodeCollectDetailMapper;
import com.wms.base.mapper.doc.DocCodeCollectMapper;
import com.wms.base.view.doc.DocCodeCollectDataView;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 码单收录单服务：生效后逐箱生成箱码台账（inv_box 待入）
 */
@Service
public class DocCodeCollectService extends AbstractM3DocService<DocCodeCollect, DocCodeCollectMapper> {

    @Resource
    private DocCodeCollectMapper docCodeCollectMapper;
    @Resource
    private DocCodeCollectDetailMapper docCodeCollectDetailMapper;
    @Resource
    private InventoryLedgerService inventoryLedgerService;

    @Override
    public DocCodeCollectMapper supplierMapper() {
        return docCodeCollectMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableInfo info = TableInfoHelper.getTableInfo(DocCodeCollect.class);
        TableInfo detailInfo = TableInfoHelper.getTableInfo(DocCodeCollectDetail.class);
        TableNode root = new TableNode();
        root.setTableClass(DocCodeCollect.class);
        root.setMapper(DocCodeCollectMapper.class);
        root.setTableName(info.getTableName());
        root.setPrimaryKey(info.getKeyColumn());

        TableNode detail = new TableNode();
        detail.setTableClass(DocCodeCollectDetail.class);
        detail.setMapper(DocCodeCollectDetailMapper.class);
        detail.setTableName(detailInfo.getTableName());
        detail.setPrimaryKey(detailInfo.getKeyColumn());
        detail.setForeignKey("collect_id");
        detail.setOrderBy(" order by seq");
        detail.setParentTable(info.getTableName());
        root.addChild(detail);
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_CODE_COLLECT;
    }

    @Override
    protected String codeColumn() {
        return "code_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocCodeCollectDataView().fetchDataView(), pageQuery);
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        List<Map<String, Object>> details = dbOldData.get(TableInfoHelper.getTableInfo(DocCodeCollectDetail.class).getTableName());
        if (details == null || details.isEmpty()) {
            throw new ServiceException("码单收录明细为空，不能生效");
        }
        for (Map<String, Object> row : details) {
            if (StrUtil.isEmpty(asStr(row.get("boxBarcode")))) {
                throw new ServiceException("存在箱条码为空的明细行，不能生效");
            }
        }
    }

    @Override
    public void afterPass(Long id, TableNode root,
                          Map<String, List<Map<String, Object>>> dbOldData,
                          Map<String, List<Map<String, Object>>> dbNewData) {
        // 生效事务内写箱码台账：逐箱待入（全链幂等，重复生效不重复累计）
        List<Map<String, Object>> details = dbNewData.get(TableInfoHelper.getTableInfo(DocCodeCollectDetail.class).getTableName());
        if (details == null) {
            return;
        }
        Map<String, Object> master = dbNewData.get(TableInfoHelper.getTableInfo(DocCodeCollect.class).getTableName()) == null
            ? null : dbNewData.get(TableInfoHelper.getTableInfo(DocCodeCollect.class).getTableName()).get(0);
        String masterBatchNo = master == null ? null : asStr(master.get("batchNo"));
        for (Map<String, Object> row : details) {
            String batchNo = StrUtil.isNotEmpty(asStr(row.get("batchNo"))) ? asStr(row.get("batchNo")) : masterBatchNo;
            inventoryLedgerService.collectBox(
                asStr(row.get("boxBarcode")),
                batchNo,
                asInt(row.get("tubes")),
                asDecimal(row.get("stdWeight")),
                asDecimal(row.get("grossWeight")),
                asDecimal(row.get("netWeight")));
        }
    }

    /** 行值统一转字符串 */
    private String asStr(Object v) {
        return v == null ? null : v.toString();
    }

    /** 行值统一转 Integer */
    private Integer asInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return new BigDecimal(v.toString()).intValue();
    }

    /** 行值统一转 BigDecimal */
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

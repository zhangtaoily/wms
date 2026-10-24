package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.DocReceiving;
import com.wms.base.mapper.doc.DocReceivingMapper;
import com.wms.base.view.doc.DocReceivingDataView;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;

/**
 * 收货清单服务：ERP 下发生成、在途流转，全程不产库存
 */
@Service
public class DocReceivingService extends AbstractM3DocService<DocReceiving, DocReceivingMapper> {

    @Resource
    private DocReceivingMapper docReceivingMapper;

    @Override
    public DocReceivingMapper supplierMapper() {
        return docReceivingMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(DocReceiving.class);
        root.setMapper(DocReceivingMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(DocReceiving.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(DocReceiving.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.WMS_RECEIVE;
    }

    @Override
    protected String codeColumn() {
        // 无单据编号列：不参与引擎取号，单号由 ERP 下发
        return null;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new DocReceivingDataView().fetchDataView(), pageQuery);
    }

    @Override
    public Tuple2<Integer, Object> save(BasicBO bo) {
        return Tuple2.of(0, "收货清单由 ERP 下发生成，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, Object> batchSave(BasicBO bo) {
        return Tuple2.of(false, "收货清单由 ERP 下发生成，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        return Tuple2.of(false, "收货清单由 ERP 下发生成，不支持删除");
    }

    @Override
    public Tuple2<Boolean, String> checkBatchDelete(Long[] ids) {
        return Tuple2.of(false, "收货清单由 ERP 下发生成，不支持删除");
    }
}

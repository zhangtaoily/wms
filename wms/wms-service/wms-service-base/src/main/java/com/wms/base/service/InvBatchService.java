package com.wms.base.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.InvBatch;
import com.wms.base.mapper.InvBatchMapper;
import com.wms.base.view.InvBatchDataView;
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
 * 库存批次引擎服务：台账只读，数据由作业生成
 */
@Service
public class InvBatchService extends BaseWmsFormService<InvBatch, InvBatchMapper> {

    @Resource
    private InvBatchMapper invBatchMapper;

    @Override
    public InvBatchMapper supplierMapper() {
        return invBatchMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(InvBatch.class);
        root.setMapper(InvBatchMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(InvBatch.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(InvBatch.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.INV_BATCH;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new InvBatchDataView().fetchDataView(), pageQuery);
    }

    @Override
    public Tuple2<Integer, Object> save(BasicBO bo) {
        return Tuple2.of(0, "台账数据由作业生成，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, Object> batchSave(BasicBO bo) {
        return Tuple2.of(false, "台账数据由作业生成，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        return Tuple2.of(false, "台账数据由作业生成，不支持删除");
    }

    @Override
    public Tuple2<Boolean, String> checkBatchDelete(Long[] ids) {
        return Tuple2.of(false, "台账数据由作业生成，不支持删除");
    }
}

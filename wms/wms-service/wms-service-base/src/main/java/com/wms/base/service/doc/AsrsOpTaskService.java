package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.AsrsOpTask;
import com.wms.base.mapper.doc.AsrsOpTaskMapper;
import com.wms.base.view.doc.AsrsOpTaskDataView;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;

/**
 * 立库作业任务服务：随仓储任务自动下发 WCS 调度，无引擎取号列
 */
@Service
public class AsrsOpTaskService extends AbstractM3DocService<AsrsOpTask, AsrsOpTaskMapper> {

    @Resource
    private AsrsOpTaskMapper asrsOpTaskMapper;

    @Override
    public AsrsOpTaskMapper supplierMapper() {
        return asrsOpTaskMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(AsrsOpTask.class);
        root.setMapper(AsrsOpTaskMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(AsrsOpTask.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(AsrsOpTask.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.ASRS_OP_TASK;
    }

    @Override
    protected String codeColumn() {
        // 无单据编号列：WCS 任务号由系统生成，不参与引擎取号
        return null;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new AsrsOpTaskDataView().fetchDataView(), pageQuery);
    }
}

package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.AsrsWhTask;
import com.wms.base.mapper.doc.AsrsWhTaskMapper;
import com.wms.base.view.doc.AsrsWhTaskDataView;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;

/**
 * 立库仓储任务服务：出库单生效自动生成，任务号 AT 开头引擎取号
 */
@Service
public class AsrsWhTaskService extends AbstractM3DocService<AsrsWhTask, AsrsWhTaskMapper> {

    @Resource
    private AsrsWhTaskMapper asrsWhTaskMapper;

    @Override
    public AsrsWhTaskMapper supplierMapper() {
        return asrsWhTaskMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(AsrsWhTask.class);
        root.setMapper(AsrsWhTaskMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(AsrsWhTask.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(AsrsWhTask.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.ASRS_WH_TASK;
    }

    @Override
    protected String codeColumn() {
        return "task_no";
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new AsrsWhTaskDataView().fetchDataView(), pageQuery);
    }
}

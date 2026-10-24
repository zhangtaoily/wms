package com.wms.base.service.doc;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.doc.AsrsOpLog;
import com.wms.base.mapper.doc.AsrsOpLogMapper;
import com.wms.base.view.doc.AsrsOpLogDataView;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.exception.ServiceException;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 立库作业日志服务：WCS 上报流水只读，按单据日期检索，不可修改
 */
@Service
public class AsrsOpLogService extends AbstractM3DocService<AsrsOpLog, AsrsOpLogMapper> {

    @Resource
    private AsrsOpLogMapper asrsOpLogMapper;

    @Override
    public AsrsOpLogMapper supplierMapper() {
        return asrsOpLogMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(AsrsOpLog.class);
        root.setMapper(AsrsOpLogMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(AsrsOpLog.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(AsrsOpLog.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.ASRS_OP_LOG;
    }

    @Override
    protected String codeColumn() {
        // 无单据编号列：日志流水由 WCS 上报生成
        return null;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new AsrsOpLogDataView().fetchDataView(), pageQuery);
    }

    @Override
    public void beforePass(Long id, TableNode root, Map<String, List<Map<String, Object>>> dbOldData) {
        throw new ServiceException("作业日志为系统上报流水，不支持生效操作");
    }

    @Override
    public Tuple2<Integer, Object> save(BasicBO bo) {
        return Tuple2.of(0, "作业日志由 WCS 上报生成，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, Object> batchSave(BasicBO bo) {
        return Tuple2.of(false, "作业日志由 WCS 上报生成，不支持手工维护");
    }

    @Override
    public Tuple2<Boolean, String> checkDelete(Long id) {
        return Tuple2.of(false, "作业日志由 WCS 上报生成，不支持删除");
    }

    @Override
    public Tuple2<Boolean, String> checkBatchDelete(Long[] ids) {
        return Tuple2.of(false, "作业日志由 WCS 上报生成，不支持删除");
    }
}

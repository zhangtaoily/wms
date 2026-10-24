package com.wms.system.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ListUtils;
import com.wms.domain.SysBusinessflowConfig;
import com.wms.domain.SysBusinessflowConfigDetail;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.systemMaintain.SysDataImportRecord;
import com.wms.domain.view.BusinessflowConfigDataView;
import com.wms.domain.view.SysDataImportRecordDataView;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.utils.TableNodeHelper;
import com.wms.system.mapper.SysBusinessflowConfigDetailMapper;
import com.wms.system.mapper.SysBusinessflowConfigMapper;
import com.wms.system.mapper.SysDataImportRecordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;

/**
 * @author li shuang
 */
@Service
public class SysDataImportRecordService extends AbstractDynamicsFormService<SysDataImportRecord, SysDataImportRecordMapper> {
    @Resource
    private SysDataImportRecordMapper sysDataImportRecordMapper;
    @Override
    public SysDataImportRecordMapper supplierMapper() {
        return  sysDataImportRecordMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(SysDataImportRecord.class);
        root.setMapper(SysDataImportRecordMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysDataImportRecord.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysDataImportRecord.class).getKeyColumn());
        root.setJoinColumns(TableNodeHelper.getCommonJoinColumnsNotAudit());
        root.setJoinTables(TableNodeHelper.getCommonJoinTablesNotAudit(root.getTableName()));
        return root;
    }
    // 对应模块
    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_DATA_IMPORT_RECORD;
    }
    // 分页查询
    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(new SysDataImportRecordDataView().fetchDataView(), pageQuery);
    }

    /**
     * 插入一条数据
     * @param userId 用户id
     * @param code 单号
     * @param isUpdate 是否更新
     * @param result 结果
     * @return int
     */
    public int insert(String userId,String code, Integer isUpdate,String result){
        SysDataImportRecord record = new SysDataImportRecord();
        record.setCode(code);
        record.setIsUpdate(isUpdate);
        record.setResult(result);
        record.setCreateBy(userId);
        return sysDataImportRecordMapper.insert(record);
    }

}

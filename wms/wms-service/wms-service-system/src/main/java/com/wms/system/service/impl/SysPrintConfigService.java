package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.api.BasicBO;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.Tuple2;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.domain.system.SysPrintConfig;
import com.wms.system.mapper.SysPrintConfigMapper;
import com.wms.system.service.ISysPrintConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class SysPrintConfigService extends AbstractDynamicsFormService<SysPrintConfig, SysPrintConfigMapper> implements ISysPrintConfigService {

    @Resource
    private SysPrintConfigMapper sysPrintConfigMapper;

    @Override
    public SysPrintConfigMapper supplierMapper() {
        return sysPrintConfigMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(SysPrintConfig.class);
        root.setMapper(SysPrintConfigMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysPrintConfig.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysPrintConfig.class).getKeyColumn());

        return root;
    }

    @Override
    public Map<String, Object> initBind() {
        return null;
    }

    @Override
    public Tuple2<Boolean, Object> copy(Long id, BasicBO bo) {
        return null;
    }

    @Override
    public Tuple2<Boolean, Object> copyAll(Long id, BasicBO bo) {
        return null;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.PRINT_CONFIG;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(SysPrintConfig.class, pageQuery);
    }

    @Override
    public List<SysPrintConfig> selectPrintConfigByModelName(String modelName) {

        LambdaQueryWrapper<SysPrintConfig> sysPrintConfigLambdaQueryWrapper = new LambdaQueryWrapper<SysPrintConfig>();
        sysPrintConfigLambdaQueryWrapper.eq(SysPrintConfig::getModelName, modelName);
        sysPrintConfigLambdaQueryWrapper.eq(SysPrintConfig::getEnabled, 1);
        return sysPrintConfigMapper.selectList(sysPrintConfigLambdaQueryWrapper);
    }
}

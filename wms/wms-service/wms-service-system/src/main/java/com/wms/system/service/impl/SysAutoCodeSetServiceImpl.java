package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.MapUtils;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.system.SysAutoCodeSet;
import com.wms.mapper.SysAutoCodeSetMapper;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.spi.ISysAutoCodeSetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.TreeMap;

@Service
public class SysAutoCodeSetServiceImpl extends AbstractDynamicsFormService<SysAutoCodeSet, SysAutoCodeSetMapper> implements ISysAutoCodeSetService {
    @Resource
    private SysAutoCodeSetMapper sysAutoCodeSetMapper;

    @Override
    public SysAutoCodeSetMapper supplierMapper() {
        return sysAutoCodeSetMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableName(TableInfoHelper.getTableInfo(SysAutoCodeSet.class).getTableName());
        root.setTableClass(SysAutoCodeSet.class);
        root.setMapper(SysAutoCodeSetMapper.class);
        root.setParentTable(null);
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysAutoCodeSet.class).getKeyColumn());
        root.setUnionColumns(MapUtils.of("function_name", "菜单名称"));

        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_AUTO_CODE_SET;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(SysAutoCodeSet.class, pageQuery);
    }

    @Override
    public SysAutoCodeSet getAutoCodeSet(FunctionEnum functionEnum) {
        LambdaQueryWrapper<SysAutoCodeSet> queryWrapper = lambdaQuery();
        queryWrapper.eq(SysAutoCodeSet::getFunctionName, functionEnum.getCode());
        queryWrapper.eq(SysAutoCodeSet::getIsAutoCode, 1);

        List<SysAutoCodeSet> sysAutoCodeSetList = list(queryWrapper);
        if (ListUtils.isEmpty(sysAutoCodeSetList)) {
            return null;
        }

        return sysAutoCodeSetList.get(0);
    }
}

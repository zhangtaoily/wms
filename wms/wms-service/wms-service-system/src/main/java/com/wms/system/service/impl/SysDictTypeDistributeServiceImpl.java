package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.domain.entity.SysDictData;
import com.wms.common.core.domain.entity.SysDictType;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ListUtils;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.system.mapper.SysDictDataDistributeMapper;
import com.wms.system.mapper.SysDictTypeDistributeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;


@Service
public class SysDictTypeDistributeServiceImpl extends AbstractDynamicsFormService<SysDictType, SysDictTypeDistributeMapper> {

    @Resource
    private SysDictTypeDistributeMapper sysDictTypeDistributeMapper;

    @Override
    public SysDictTypeDistributeMapper supplierMapper() {
        return sysDictTypeDistributeMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableName(TableInfoHelper.getTableInfo(SysDictType.class).getTableName());
        root.setTableClass(SysDictType.class);
        root.setMapper(SysDictTypeDistributeMapper.class);
        root.setParentTable(null);
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysDictType.class).getKeyColumn());

        TableNode child = new TableNode();
        child.setTableName(TableInfoHelper.getTableInfo(SysDictData.class).getTableName());
        child.setTableClass(SysDictData.class);
        child.setMapper(SysDictDataDistributeMapper.class);
        child.setParentTable(TableInfoHelper.getTableInfo(SysDictType.class).getTableName());
        child.setPrimaryKey(TableInfoHelper.getTableInfo(SysDictData.class).getKeyColumn());
        child.setForeignKey("dict_type_id");

        root.setChildren(ListUtils.of(child));

        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_DICT_DATA;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return null;
    }
}



package com.wms.system.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.ListUtils;
import com.wms.domain.SysBusinessflowConfig;
import com.wms.domain.SysBusinessflowConfigDetail;
import com.wms.domain.SysFieldConfig;
import com.wms.domain.view.BusinessflowConfigDataView;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.AbstractDynamicsFormService;
import com.wms.service.common.service.TableNode;
import com.wms.service.common.utils.TableNodeHelper;
import com.wms.system.mapper.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;

@Service
public class SysBusinessflowConfigService extends AbstractDynamicsFormService<SysBusinessflowConfig, SysBusinessflowConfigMapper> {
    @Resource
    private SysBusinessflowConfigMapper sysBusinessflowConfigMapper;
    @Override
    public SysBusinessflowConfigMapper supplierMapper() {
        return sysBusinessflowConfigMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        //主表
        TableNode root = new TableNode();
        root.setTableClass(SysBusinessflowConfig.class);
        root.setMapper(SysBusinessflowConfigMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysBusinessflowConfig.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysBusinessflowConfig.class).getKeyColumn());
        root.setJoinColumns(TableNodeHelper.getCommonJoinColumnsNotAudit());
        root.setJoinTables(TableNodeHelper.getCommonJoinTablesNotAudit(root.getTableName()));

        //子表请求头
        TableNode detail = new TableNode();
        detail.setTableClass(SysBusinessflowConfigDetail.class);
        detail.setParentTable(TableInfoHelper.getTableInfo(SysBusinessflowConfig.class).getTableName());
        detail.setMapper(SysBusinessflowConfigDetailMapper.class);
        detail.setForeignKey("business_id");
        detail.setTableName(TableInfoHelper.getTableInfo(SysBusinessflowConfigDetail.class).getTableName());
        detail.setPrimaryKey(TableInfoHelper.getTableInfo(SysBusinessflowConfigDetail.class).getKeyColumn());
        detail.setJoinColumns("sys_dict_data.dict_label orgName");
        detail.setJoinTables(" left join sys_dict_data on sys_dict_data.dict_value=sys_businessflow_config_detail.org_id and  sys_dict_data.dict_type='interface_org'");
        detail.setOrderBy(" order by sort_index ASC ");
        //主表添加子表
        root.setChildren(ListUtils.of(detail));
        return root;
    }
    ///对应模块
    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.SYS_BUSINESSFLOW_CFG;
    }
    ///分页查询
    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        BusinessflowConfigDataView configDataView = new BusinessflowConfigDataView();
        return super.queryTablePageList(configDataView.fetchDataView(), pageQuery);
    }

}

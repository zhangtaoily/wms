package com.wms.base.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.SysPrintTemplate;
import com.wms.base.mapper.SysPrintTemplateMapper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;

/**
 * 打印模板引擎服务
 */
@Service
public class SysPrintTemplateService extends BaseWmsFormService<SysPrintTemplate, SysPrintTemplateMapper> {

    @Resource
    private SysPrintTemplateMapper printTemplateMapper;

    @Override
    public SysPrintTemplateMapper supplierMapper() {
        return printTemplateMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(SysPrintTemplate.class);
        root.setMapper(SysPrintTemplateMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysPrintTemplate.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysPrintTemplate.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.PRINT_TEMPLATE;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(SysPrintTemplate.class, pageQuery);
    }
}

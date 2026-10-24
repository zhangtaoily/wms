package com.wms.base.service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wms.base.domain.SysRemindSetting;
import com.wms.base.mapper.SysRemindSettingMapper;
import com.wms.common.core.domain.PageQuery;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;
import com.wms.page.DynamicsTableDataInfo;
import com.wms.service.common.service.TableNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.TreeMap;

/**
 * 提醒设置引擎服务
 */
@Service
public class SysRemindSettingService extends BaseWmsFormService<SysRemindSetting, SysRemindSettingMapper> {

    @Resource
    private SysRemindSettingMapper remindSettingMapper;

    @Override
    public SysRemindSettingMapper supplierMapper() {
        return remindSettingMapper;
    }

    @Override
    public TableNode doFetchTableNode() {
        TableNode root = new TableNode();
        root.setTableClass(SysRemindSetting.class);
        root.setMapper(SysRemindSettingMapper.class);
        root.setTableName(TableInfoHelper.getTableInfo(SysRemindSetting.class).getTableName());
        root.setPrimaryKey(TableInfoHelper.getTableInfo(SysRemindSetting.class).getKeyColumn());
        return root;
    }

    @Override
    public FunctionEnum getSupportType() {
        return FunctionEnum.REMIND_SETTING;
    }

    @Override
    public DynamicsTableDataInfo<TreeMap<String, Object>, SysFieldConfig> queryPageList(PageQuery pageQuery) {
        return super.queryTablePageList(SysRemindSetting.class, pageQuery);
    }
}

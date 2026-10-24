package com.wms.spi;

import com.wms.common.core.domain.PageQuery;
import com.wms.common.core.page.TableDataInfo;
import com.wms.common.enums.FunctionEnum;
import com.wms.domain.SysFieldConfig;

import java.util.List;
import java.util.Map;

public interface ISysFieldConfigService {

    List<SysFieldConfig> listDynamicField(FunctionEnum name);

    List<SysFieldConfig> listSearchField(FunctionEnum name);

    List<SysFieldConfig> listField(FunctionEnum name);

    /**
     * PDA 引擎字段配置（M6）：mobile_show=1 的列按 mobile_seq 升序返回
     */
    List<Map<String, Object>> listMobileField(FunctionEnum name);


    List<String> getWrapperColumns(FunctionEnum name);

    /**
     * 分页查询字段配置（管理页）
     */
    TableDataInfo<SysFieldConfig> pageFieldConfig(SysFieldConfig fieldConfig, PageQuery pageQuery);

    SysFieldConfig selectFieldConfigById(Long id);

    int insertFieldConfig(SysFieldConfig fieldConfig);

    int updateFieldConfig(SysFieldConfig fieldConfig);

    int deleteFieldConfigByIds(Long[] ids);
}

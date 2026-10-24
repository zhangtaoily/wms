package com.wms.mapper;

import com.wms.domain.WfConfig;
import com.wms.domain.system.SystemMenu;
import com.wms.mapper.CustomizeMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface WfConfigMapper extends CustomizeMapper<WfConfig> {

    /**
     * 查询工作流配置
     *
     * @return
     */
    @Select("select * from wf_config where `function_name`=#{functionName} and enabled=1 limit 1")
    WfConfig getWfFunctionName(@Param("functionName") String functionName);

}

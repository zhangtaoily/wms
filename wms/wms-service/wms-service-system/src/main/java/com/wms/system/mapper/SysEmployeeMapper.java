package com.wms.system.mapper;

import com.wms.domain.system.SysEmployee;
import com.wms.mapper.CustomizeMapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

public interface SysEmployeeMapper extends CustomizeMapper<SysEmployee> {

    @Select("select max(hrmodify_time) from sys_employee")
    LocalDateTime selectMaxOaModifiedTime();
}

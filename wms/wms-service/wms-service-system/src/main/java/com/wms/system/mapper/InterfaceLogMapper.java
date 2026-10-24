package com.wms.system.mapper;

import com.wms.domain.SysInterfaceLog;
import com.wms.mapper.CustomizeMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface InterfaceLogMapper extends CustomizeMapper<SysInterfaceLog> {

    List<SysInterfaceLog> selectPageByUser(@Param("offset") Long offset, @Param("size") Long size, @Param("userId") Long userId, @Param("date") LocalDate date);

}

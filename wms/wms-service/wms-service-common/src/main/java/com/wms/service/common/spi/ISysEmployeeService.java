package com.wms.service.common.spi;

import com.wms.common.utils.Tuple2;
import com.wms.domain.system.SysEmployee;

public interface ISysEmployeeService {

    SysEmployee getSysEmployee(Long id);

    Tuple2<Boolean, Object> upsert(SysEmployee employee);

}

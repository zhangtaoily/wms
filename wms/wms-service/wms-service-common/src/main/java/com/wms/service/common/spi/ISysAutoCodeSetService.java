package com.wms.service.common.spi;

import com.wms.common.enums.FunctionEnum;
import com.wms.domain.system.SysAutoCodeSet;

public interface ISysAutoCodeSetService {

    SysAutoCodeSet getAutoCodeSet(FunctionEnum functionEnum);

}

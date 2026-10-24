package com.wms.service.common.spi;

import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.Tuple2;

import java.util.List;
import java.util.Map;

public interface ICallBack {
    Tuple2<Boolean, String> CallBackCommon(FunctionEnum AFunEnum,
                                           Map<String, List<Map<String, Object>>> AOrgEntity,
                                           Map<String, List<Map<String, Object>>> AEntity,
                                           Boolean executionSelfWriteBack);

}

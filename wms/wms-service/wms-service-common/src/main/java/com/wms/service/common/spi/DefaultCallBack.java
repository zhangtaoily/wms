package com.wms.service.common.spi;

import com.wms.common.enums.FunctionEnum;
import com.wms.common.utils.Tuple2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 业务反写回调默认空实现。
 * WMS 当前无跨模块单据反写需求，所有单据动作直接放行；
 * 后续如需反写（如出库回写通知单），实现 ICallBack 并注册为 Spring Bean 即可覆盖本默认实现。
 *
 * @author wms
 */
@Component
public class DefaultCallBack implements ICallBack {

    @Override
    public Tuple2<Boolean, String> CallBackCommon(FunctionEnum AFunEnum,
                                                  Map<String, List<Map<String, Object>>> AOrgEntity,
                                                  Map<String, List<Map<String, Object>>> AEntity,
                                                  Boolean executionSelfWriteBack) {
        return Tuple2.of(true, "");
    }
}

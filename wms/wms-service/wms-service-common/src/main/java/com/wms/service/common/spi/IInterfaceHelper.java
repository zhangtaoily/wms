package com.wms.service.common.spi;

import com.wms.common.core.domain.R;
import com.wms.common.utils.Tuple2;
import com.wms.service.common.service.TableNode;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface IInterfaceHelper {

    void setAPIInfo(Map<String, List<Map<String, Object>>> dbData, TableNode tableNode, Long pactId);

    Tuple2<Boolean, String> setAPIInfo(Map<String, List<Map<String, Object>>> dbData, TableNode tableNode, Long pactId, int workTiming);

    Tuple2<Boolean, String> setAPIInfo(Map<String, List<Map<String, Object>>> dbData, TableNode tableNode, Long pactId, int workTiming, JSONObject customParamJson);

    R ReSend(Long logId);
}

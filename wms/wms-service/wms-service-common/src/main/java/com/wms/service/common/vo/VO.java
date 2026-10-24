package com.wms.service.common.vo;

import com.wms.common.utils.JsonUtils;

public interface VO {
   default String toJsonString(){
       return JsonUtils.toJsonString(this);
   }
}

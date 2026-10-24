package com.wms.common.api;

/**
 * 检查接口，表明对象需要进行检查
 * - 场景1：前端接口请求时，对入参检查
 */
public interface ValidateAble {
    void validate();
}

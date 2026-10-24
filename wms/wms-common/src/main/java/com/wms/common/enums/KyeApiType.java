package com.wms.common.enums;

import lombok.Getter;

/**
 * ERP单据key枚举
 * @author li_shuang
 */

@Getter
public enum KyeApiType {

    BATCH_ORDER("open.api.openCommon.batchOrder","跨越下单接口"),

    WAYBILL_STATUS_GET("open.api.common.waybillStatus.get","跨越快运查询状态服务"),

    QUERY_PUBLIC_ROUTE("open.api.openCommon.queryPublicRoute","跨越快运路由查询服务"),

    CANCEL_ORDER("open.api.openCommon.cancelOrder","跨越快运取消运单接口"),

    QUERY_FREIGHT_CHARGE("open.api.openCommon.queryFreightCharge","预估运费接口"),

    APPOINT_UPLOAD("open.api.openCommon.appointUpload","上传文件接口");

    private final String value;
    private final String description;

    KyeApiType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.name();
    }
}


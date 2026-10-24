package com.wms.common.enums;

import lombok.Getter;

/**
 * ERP单据key枚举
 * @author li_shuang
 */

@Getter
public enum WaybillType {

    PICKUP_DEPARTURE(0, "出车取货"),
    PICKUP_SIGN(1, "取货签到"),
    PICKUP_COMPLETE(2, "揽件完毕"),
    HANDOVER_TAKE(3, "交接扫描(取)"),
    TRANSIT_UNLOAD_TAKE(4, "中转卸货(取)"),
    TRUNK_DEPARTURE(45, "干线发出"),
    TRUNK_ARRIVAL(35, "干线到达"),
    SECURITY_SCAN(160, "安检扫描"),
    AIRPORT_DEPARTURE(5, "机场发件"),
    CHARTER_DEPARTURE(6, "包车发件"),
    PICKUP_COMPLETED(7, "提货完毕"),
    TRANSIT_UNLOAD_DELIVERY(90, "中转卸货(派)"),
    HANDOVER_DELIVERY(8, "交接扫描(派)"),
    READY_DELIVERY(98, "准备派送"),
    DELIVERY_SIGN(99, "派送签到"),
    DELIVERY_EXCEPTION(-1, "派送异常"),
    SIGNED(100, "签收完毕"),
    RETURN_BLOCK(1001, "退拦单"),
    CANCELED(-2, "已取消"),
    NO_INFO(-3, "暂无物流信息");



    private final int code;

    private final String value;

    WaybillType(int code,String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.name();
    }
    public static String getValueByCode(int code) {
        for (WaybillType node : WaybillType.values()) {
            if (node.getCode() == code) {
                return node.getValue();
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

